package com.github.danielchemko.winmdj.parser

import com.github.danielchemko.winmdj.core.autoobject.model.CLRMetadataType
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

/**
 * Data structures that are relevant for the parsing and interpretation of a given WinMD file. These objects aren't
 * expected to be used commonly for interpreting an MD file as an outside observer. The code/MdObjects are designed for
 * that purpose.
 */

sealed interface WinMdParseStruct {
    fun length(): UInt {
        return length(this::class)
    }

    fun length(refClass: KClass<*>): UInt {
        return refClass.constructors.first().parameters.sumOf { typeParam ->
            when (typeParam.type.classifier) {
                List::class -> {
                    val listValueClass = typeParam.type.arguments[0].type!!.classifier as KClass<*>
                    typeParam.findAnnotation<Length>()
                        ?.let { it.length * length(listValueClass).toInt() }
                        ?: throw IllegalStateException("Unable to size type: ${typeParam.name}")
                }

                ByteArray::class -> {
                    typeParam.findAnnotation<Length>()?.length
                        ?: throw IllegalStateException("Unable to size type: ${typeParam.name}")
                }

                UByte::class -> 1
                UShort::class -> 2
                UInt::class -> 4
                ULong::class -> 8
                else -> {
                    val clazz = typeParam.type.classifier as KClass<*>
                    if (WinMdParseStruct::class.isSuperclassOf(clazz)) {
                        length(clazz).toInt()
                    } else {
                        throw IllegalStateException("Unable to size type: ${typeParam.name}")
                    }
                }
            }
        }.toUInt()
    }
}

data class DosHeader(
    val signature: UShort,
    val cblp: UShort,
    val cp: UShort,
    val crlc: UShort,
    val cparhdr: UShort,
    val minalloc: UShort,
    val maxalloc: UShort,
    val ss: UShort,
    val sp: UShort,
    val csum: UShort,
    val ip: UShort,
    val cs: UShort,
    val lfarlc: UShort,
    val ovno: UShort,
    @Length(4) val res: List<UShort>,
    val oemId: UShort,
    val oemInfo: UShort,
    @Length(10) val res2: List<UShort>,
    val lfanew: UInt,
) : WinMdParseStruct

data class NTHeader32(
    val signature: UInt,
    val fileHeader: NtFileHeader,
    val optionalHeader: NtOptionalHeader
) : WinMdParseStruct

data class NTHeader64(
    val signature: UInt,
    val fileHeader: NtFileHeader,
    val optionalHeader: NtOptionalHeader64
) : WinMdParseStruct

data class SectionHeader(
    @Length(8)
    val name: ByteArray,
    val physicalAddressOrVirtualSize: UInt, // Union of PhysicalAddress and VirtualSize
    val virtualAddress: UInt,
    val sizeOfRawData: UInt,
    val rawDataPtr: UInt,
    val relocationsPtr: UInt,
    val lineNumbersPtr: UInt,
    val relocationCount: UShort,
    val lineNumberCount: UShort,
    val characteristics: UInt,
) : WinMdParseStruct {
    fun shortName(): String {
        return String(name)
    }
}

data class NtFileHeader(
    val machine: UShort,
    val sectionCount: UShort,
    val timestamp: UInt,
    val symbolTablePtr: UInt,
    val symbolCount: UInt,
    val optionalHeaderLength: UShort,
    val characteristics: UShort,
) : WinMdParseStruct

data class NtOptionalHeader(
    val magic: UShort,
    val majorLinker: UByte,
    val minorLinker: UByte,
    val codeSize: UInt,
    val initializedCodeSize: UInt,
    val uninitializedCodeSize: UInt,
    val entryPointPtr: UInt,
    val baseCodePtr: UInt,
    val baseDataPtr: UInt,
    val imageBasePtr: UInt,
    val sectionAlignment: UInt,
    val fileAlignment: UInt,
    val majorOperatingSystem: UShort,
    val minorOperatingSystem: UShort,
    val majorImageVersion: UShort,
    val minorImageVersion: UShort,
    val majorSubsystemVersion: UShort,
    val minorSubsystemVersion: UShort,
    val win32Version: UInt,
    val sizeOfImage: UInt,
    val sizeOfHeaders: UInt,
    val checksum: UInt,
    val subSystem: UShort,
    val dllCharacteristics: UShort,
    val stackReserveLength: UInt,
    val stackCommitLength: UInt,
    val heapReserveLength: UInt,
    val heapCommitLength: UInt,
    val loaderFlags: UInt,
    val rvaAndSizesCount: UInt,
    @Length(16)
    val sectionPointers: List<ReadRegion>
) : WinMdParseStruct

data class NtOptionalHeader64(
    val magic: UShort,
    val majorLinker: UByte,
    val minorLinker: UByte,
    val codeSize: UInt,
    val initializedCodeSize: UInt,
    val uninitializedCodeSize: UInt,
    val entryPointPtr: UInt,
    val baseCodePtr: UInt,
    val imageBasePtr: ULong,
    val sectionAlignment: UInt,
    val fileAlignment: UInt,
    val majorOperatingSystem: UShort,
    val minorOperatingSystem: UShort,
    val majorImageVersion: UShort,
    val minorImageVersion: UShort,
    val majorSubsystemVersion: UShort,
    val minorSubsystemVersion: UShort,
    val win32Version: UInt,
    val sizeOfImage: UInt,
    val sizeOfHeaders: UInt,
    val checksum: UInt,
    val subSystem: UShort,
    val dllCharacteristics: UShort,
    val stackReserveLength: ULong,
    val stackCommitLength: ULong,
    val heapReserveLength: ULong,
    val heapCommitLength: ULong,
    val loaderFlags: UInt,
    val rvaAndSizesCount: UInt,
    @Length(16)
    val sectionPointers: List<ReadRegion>
) : WinMdParseStruct

/* .cormeta CLR 2.0 */
data class ImageCor20(
    val cb: UInt,
    val majorRuntimeVersion: UShort,
    val minorRuntimeVersion: UShort,
    val metadata: ReadRegion,
    val flags: UInt,
    val entryPointTokenOrRva: UInt, // Union of EntryPointToken and EntryPointRVA
    val resources: ReadRegion,
    val strongNameSignature: ReadRegion,
    val codeManagerTable: ReadRegion,
    val vtableFixes: ReadRegion,
    val exportAddressTableJumps: ReadRegion,
    val managedNativeHeader: ReadRegion,
) : WinMdParseStruct

data class ReadRegion(
    val virtualAddress: UInt,
    val size: UInt,
) : WinMdParseStruct

data class StreamRange(
    var offset: UInt,
    val size: UInt,
) : WinMdParseStruct {
    fun addOffset(addedOffset: Int): StreamRange {
        offset += addedOffset.toUInt()
        return this
    }

    fun seek(byteBuffer: ByteBuffer, relativeOffset: Int) {
        byteBuffer.position(this.offset.toInt() + relativeOffset)
    }
}

data class StreamName(
    @Length(12)
    val name: ByteArray
) : WinMdParseStruct {
    fun paddedBytesUsed(): Int {
        var padding = 4 - (toSafeString().length % 4)
        if (padding == 4) {
            padding = 4
        }
        return padding + toSafeString().length
    }

    fun toSafeString(): String {
        val dirtyString = String(name, StandardCharsets.US_ASCII)
        val firstNull = dirtyString.indexOf(0.toChar())
        return if (firstNull > -1) {
            dirtyString.substring(0, firstNull)
        } else {
            dirtyString
        }
    }
}

data class ColumnLayout(val columnSizes: List<Int>, val rowWidth: Int, val columnOffsets: List<Int>) {
    companion object {
        fun from(columnSizes: Array<Int>): ColumnLayout {
            val offsets = IntArray(columnSizes.size)
            offsets[0] = 0
            (1 until columnSizes.size).forEach { idx ->
                offsets[idx] = offsets[idx - 1] + columnSizes[idx - 1]
            }

            return ColumnLayout(columnSizes.toList(), columnSizes.sumOf { it }, offsets.toList())
        }
    }
}

data class NavigationTable(
    val type: CLRMetadataType,
    val rowCount: Int,
) {
    lateinit var columnLayout: ColumnLayout
    var startIndex: Int = 0
    var size: Int = 0

    override fun toString(): String {
        return "${
            type.bitSetIndex.toString().padStart(2, '0')
        } $type Rows:[$rowCount] Columns:[${columnLayout.columnSizes}] SizeOnDisk:[${size}]"
    }

    fun seek(byteBuffer: ByteBuffer, offset: Int) {
        byteBuffer.position(startIndex + offset)
    }
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Length(val length: Int)
