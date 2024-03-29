package com.github.danielchemko.winmdj.util

import com.github.danielchemko.winmdj.parser.Length
import com.github.danielchemko.winmdj.parser.WinMdParseStruct
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

private val PRIMITIVE_TYPES = setOf(UByte::class, UShort::class, UInt::class, ULong::class)

/** Debugging around the stream position to get a feel for the data surrounding it */
@OptIn(ExperimentalStdlibApi::class)
fun explore(before: Int, byteBuffer: ByteBuffer, after: Int, hex: Boolean = false, prefix: String = "") {
    val originalPosition = byteBuffer.position()
    try {
        val position = max(0, originalPosition - before)
        val readBuffer = ByteArray(after + originalPosition - position)
        byteBuffer.position(position)
        byteBuffer.get(readBuffer)
        if (hex) {
            println("$prefix[${readBuffer.toHexString(HexFormat.UpperCase)}]")
            println(
                "${(0 until prefix.length + 1).joinToString(separator = "") { " " }}${
                    (0 until (originalPosition - position)).joinToString(
                        separator = ""
                    ) { "  " }
                }^ $originalPosition (${originalPosition.toHexString(HexFormat.UpperCase)})"
            )
        } else {
            println("$prefix[${String(readBuffer, StandardCharsets.US_ASCII)}]")
            println(
                "${(0 until prefix.length + 1).joinToString(separator = "") { " " }}${
                    (0 until (originalPosition - position)).joinToString(
                        separator = ""
                    ) { " " }
                }^ $originalPosition (${originalPosition.toHexString(HexFormat.UpperCase)})"
            )
        }
    } finally {
        byteBuffer.position(originalPosition)
    }
}

/** Construct the object (struct) by reading bytes off the byte buffer using the constructor as a definition of the
 * data layout in the buffer. For byte arrays and lists, we use a @Length annotation to indicate how many repeats
 * are expected in the field. */
fun <T : Any> fillObject(
    byteBuffer: ByteBuffer,
    kClass: KClass<T>
): T {
    val constructor = kClass.constructors.firstOrNull()
        ?: throw IllegalStateException("Unable to fill object $kClass because it has no constructor")

    if (isPrimitive(kClass)) {
        return parsePrimitive(byteBuffer, kClass)
    }

    val values = constructor.parameters.map { param ->
        return@map when (param.type.classifier) {
            ByteArray::class -> {
                val lengthAnnotation: Length =
                    param.annotations.filterIsInstance<Length>().firstOrNull()
                        ?: throw IllegalArgumentException("Unable to interpret [$kClass] property [${param.name}] because it is a ByteArray without a @Length() annotation")
                val array = ByteArray(lengthAnnotation.length)
                byteBuffer.get(array)
                param to array
            }

            List::class -> {
                val lengthAnnotation: Length =
                    param.annotations.filterIsInstance<Length>().firstOrNull()
                        ?: throw IllegalArgumentException("Unable to interpret [$kClass] property [${param.name}] because it is a List<> without a @Length() annotation")


                param to (0 until lengthAnnotation.length).map {
                    fillObject(
                        byteBuffer,
                        param.type.arguments[0].type!!.classifier as KClass<*>
                    )
                }.toList()
            }

            else -> {
                if (isPrimitive(param.type.classifier as KClass<*>)) {
                    return@map param to parsePrimitive(byteBuffer, param.type.classifier as KClass<*>)
                }

                val typeClazz = param.type.classifier as KClass<*>
                if (WinMdParseStruct::class.isSuperclassOf(typeClazz)) {
                    return@map param to fillObject(byteBuffer, typeClazz) // How to pass on maximum limits?
                }

                throw IllegalArgumentException("Unable to interpret class [$kClass] property [${param.name}] (${param.type.classifier}) to read")
            }
        }
    }.toMap()

    return constructor.callBy(values)
}

private fun isPrimitive(kClass: KClass<*>) = PRIMITIVE_TYPES.contains(kClass)

@Suppress("UNCHECKED_CAST") // This is actually safe Kotlin, figure it out!
fun <T : Any> parsePrimitive(byteBuffer: ByteBuffer, kClass: KClass<T>): T {
    return when (kClass) {
        UByte::class -> byteBuffer.get().toUByte() as T
        UShort::class -> byteBuffer.short.toUShort() as T
        UInt::class -> byteBuffer.int.toUInt() as T
        ULong::class -> byteBuffer.long.toULong() as T
        else -> throw IllegalArgumentException("Unable to interpret primitive class [$kClass] to read")
    }
}