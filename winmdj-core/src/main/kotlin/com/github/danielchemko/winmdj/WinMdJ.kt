package com.github.danielchemko.winmdj

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.ObjectType
import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.text.HexFormat

private val EXCLUDED_FUNCTIONS = setOf(
    "equals",
    "clone",
    "toString",
    "hashCode",
    "copy",
    "getByte",
    "getShort",
    "getDouble",
    "getUByte",
    "getULong",
    "getFloat",
    "getLong",
    "getChar",
    "getInt",
    "getUInt",
    "getAsBoolean",
    "getUShort",
    "getString",
    "getValueBlob",
    "getValueClass",
    "getStub",
)

@OptIn(ExperimentalStdlibApi::class)
fun main(vararg args: String) {
    val navigator = WinMdNavigator()
    navigator.parseFile(Path.of("Windows.Win32.winmd"))
//    navigator.parseFile(Path.of("C:/Users/dchemko/Desktop/ILSpy_selfcontained_8.2.0.7535-x64/System.Private.CoreLib.dll"))

//    val path = Path.of("C:/Windows/System32")
//    path.toFile().list().filter { it.contains(".dll") || it.contains(".exe") }.map { path.resolve(it) }.forEach {
//        try {
//            println(it.name)
//            navigator.parseFile(it)
//        } catch (e: Exception) {
//            println(e.message)
//        }
//    }
//    val path = Path.of("C:/Windows/System32/srmlib.dll")
//    navigator.parseFile(path)


    val objectMapper = MdObjectMapper(navigator)

//    objectMapper.getCursor(Constant::class.java).forEach { obj ->
//        try {
//            println("Constant/${obj.getToken()}/${obj.getType()} -- ${obj.getValue()}")
//            successes++
//        } catch (e: Exception) {
//            failures++
////            println("StubConstantImpl/${obj.getToken().toUInt().toHexString(HexFormat.UpperCase)} -- !!FAILED!! -- ${e.message}")
//        }
//    }

    var successes = 0
    var failures = 0
    val showMessage = false

    val failureCombinations = ConcurrentHashMap<KClass<*>, ConcurrentHashMap<String, AtomicLong>>()

    val printOkFunctions = mutableSetOf<KFunction<*>>()
    val printingOkFunctions = mutableMapOf<KFunction<*>, AtomicInteger>()
    val badFunctions = mutableSetOf<KFunction<*>>()
    val failingFunctions = mutableMapOf<KFunction<*>, AtomicInteger>()

    WinMdObject::class.sealedSubclasses.sortedBy { it.findAnnotation<ObjectType>()!!.objectType.bitSetIndex }
        .forEach { clazz ->
            objectMapper.getCursor(clazz.java).forEach { obj ->
                clazz.functions.filterNot { EXCLUDED_FUNCTIONS.contains(it.name) }.forEach { func ->
                    if (badFunctions.contains(func)) { // || printOkFunctions.contains(func)
                        return@forEach
                    }

                    try {
                        val result = func.call(obj);

                        val resultStr = parseValueToString(result ?: "")

                        if (!printOkFunctions.contains(func)) {
                            println(
                                "${clazz.simpleName}/${
                                    obj.getToken().toHexString(HexFormat.UpperCase)
                                }/${func.name} -- $resultStr"
                            )
                            if (printingOkFunctions.computeIfAbsent(func) { AtomicInteger() }.incrementAndGet() > 15) {
                                printOkFunctions.add(func)
                            }
                        }
                        successes++
                    } catch (e: Exception) {
                        println(
                            "${clazz.simpleName}/${
                                obj.getToken().toHexString(HexFormat.UpperCase)
                            }/${func.name} -- !!FAILED!! -- ${e.message}"
                        )
                        failures++
                        failureCombinations.computeIfAbsent(clazz) { ConcurrentHashMap() }
                            .computeIfAbsent(func.name) { AtomicLong() }
                            .incrementAndGet()
                        if (failingFunctions.computeIfAbsent(func) { AtomicInteger() }.incrementAndGet() > 15) {
                            badFunctions.add(func)
                        }
                    }
                }
            }
        }
    println(
        "Success: $successes, Failures: $failures -- FailureSets: \n\n${
            failureCombinations.map { "${it.key.simpleName} -- ${it.value}" }.joinToString("\n")
        }"
    )
}

@OptIn(ExperimentalStdlibApi::class)
fun parseValueToString(result: Any): Any {
    return when (result) {
        is UByte -> "$result (${result.toHexString(HexFormat.UpperCase)})"
        is UShort -> "$result (${result.toHexString(HexFormat.UpperCase)})"
        is UInt -> "$result (${result.toHexString(HexFormat.UpperCase)})"
        is ULong -> "$result (${result.toHexString(HexFormat.UpperCase)})"
        is ByteArray -> "ByteArray [${result.toHexString(HexFormat.UpperCase)}]"
        is WinMdObject -> "Type: ${result::class.simpleName}/${result.getToken()} (${result.getOffset()})"
        is List<*> -> result.map { parseValueToString(it ?: "") }
            .joinToString(separator = ",", prefix = "[", postfix = "]")

        is Array<*> -> result.map { parseValueToString(it ?: "") }
            .joinToString(separator = ",", prefix = "[", postfix = "]")

        else -> result.toString()
    }
}
