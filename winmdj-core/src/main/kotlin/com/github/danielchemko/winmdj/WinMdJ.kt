package com.github.danielchemko.winmdj

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.ObjectType
import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import com.github.danielchemko.winmdj.parser.NavigatorQuirk
import com.github.danielchemko.winmdj.parser.ResolutionScopeAsShort
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

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


/**
 * This is a test runner to perform sanity testing of this solution over many different COFF/WinMD compatible CLR table
 * files. This isn't intended to do anything valuable, just sanity test that this solution should be able to decode the
 * file specified.
 */
fun main(vararg args: String) {
    processFile(Path.of("Windows.Win32.winmd"), ResolutionScopeAsShort)
    processFile(Path.of("C:/Program Files/ILSpy_selfcontained_8.2.0.7535-x64/System.Private.CoreLib.dll"))
    processFile(Path.of("C:/Program Files/ILSpy_selfcontained_8.2.0.7535-x64/WindowsBase.dll"))

    val path = Path.of("C:/Windows/System32")
    path.toFile().list()!!.filter { it.contains(".dll") || it.contains(".exe") }.map { path.resolve(it) }.forEach {
        try {
            processFile(it)
        } catch (e: Exception) {
            println(e.message)
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun processFile(path: Path, vararg quirks: NavigatorQuirk): WinMdNavigator {
    val navigator = WinMdNavigator(quirks.toSet())
    navigator.parseFile(path)
    val objectMapper = MdObjectMapper(navigator)
    var successes = 0
    var failures = 0

    val failureCombinations = ConcurrentHashMap<KClass<*>, ConcurrentHashMap<String, AtomicLong>>()

    val printOkFunctions = mutableSetOf<KFunction<*>>()
    val printingOkFunctions = mutableMapOf<KFunction<*>, AtomicInteger>()
    val badFunctions = mutableSetOf<KFunction<*>>()
    val failingFunctions = mutableMapOf<KFunction<*>, AtomicInteger>()

    val printErrors = true
    val printOk = true

    WinMdObject::class.sealedSubclasses.sortedBy { it.findAnnotation<ObjectType>()!!.objectType.bitSetIndex }
        .forEach { clazz ->
            objectMapper.getCursor(clazz.java).forEach { obj ->
                clazz.functions.filterNot { EXCLUDED_FUNCTIONS.contains(it.name) }.forEach { func ->
                    if (badFunctions.contains(func)) { // || printOkFunctions.contains(func)
                        return@forEach
                    }

                    try {
                        val result = func.call(obj);

                        if (!printOkFunctions.contains(func)) {
                            val resultStr = parseValueToString(result ?: "<null>")
                            if (printOk) {
                                println(
                                    "${clazz.simpleName}/${
                                        obj.getToken().toHexString(HexFormat.UpperCase)
                                    }/${func.name} -- $resultStr"
                                )
                            }
                            if (printingOkFunctions.computeIfAbsent(func) { AtomicInteger() }.incrementAndGet() > 15) {
                                printOkFunctions.add(func)
                            }
                        }
                        successes++
                    } catch (e: Exception) {
                        if (printErrors) {
                            println(
                                "${clazz.simpleName}/${
                                    obj.getToken().toHexString(HexFormat.UpperCase)
                                }/${func.name} -- !!FAILED!! -- ${e.message}"
                            )
                        }
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

    return navigator
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
