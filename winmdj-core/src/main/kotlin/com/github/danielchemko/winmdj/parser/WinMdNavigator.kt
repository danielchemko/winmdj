package com.github.danielchemko.winmdj.parser

import com.github.danielchemko.winmdj.core.mdspec.*
import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType.*
import com.github.danielchemko.winmdj.util.convertToInt
import com.github.danielchemko.winmdj.util.fillObject
import com.github.danielchemko.winmdj.util.parsePrimitive
import java.io.Closeable
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.and
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

private val PE_HEADER_MAGIC = 0x10B.toUShort()
private val PE_PLUS_HEADER_MAGIC = 0x20B.toUShort()
private const val CLI_METADATA_MAGIC = 0x424a5342
private const val STREAM_STRINGS = "#Strings"
private const val STREAM_BLOB = "#Blob"
private const val STREAM_GUID = "#GUID"
private const val STREAM_TABLE = "#~"
private const val STREAM_USER_STRINGS = "#US"
private val ZERO_BYTE: Byte = 0.toByte()
private val ONE_BYTE: Byte = 1.toByte()

/* Maybe some day this will be able to parse an entire COFF file, but not today */
private val SECTION_EXPORTS = 0
private val SECTION_IMPORTS = 1
private val SECTION_RESOURCE = 2
private val SECTION_EXCEPTION = 3
private val SECTION_CERTIFICATE = 4
private val SECTION_BASE_RELOCATION = 5
private val SECTION_DEBUG = 6
private val SECTION_ARCH = 7
private val SECTION_GLOBAL_POINTER = 8
private val SECTION_TLS_TABLE = 9
private val SECTION_LOAD_CONFIG_TABLE = 10
private val SECTION_BOUND_IMPORT = 11
private val SECTION_IAT = 12
private val SECTION_DELAY_IMPORT_DESCRIPTOR = 13
private val SECTION_CLR = 14
private val SECTION_RESERVED = 14

public sealed interface NavigatorQuirk

public object ResolutionScopeAsShort : NavigatorQuirk

/* Coded index bits for short coded indices */
private val bitMaskShort = arrayOf(1.toUShort(), 3.toUShort(), 7.toUShort(), 15.toUShort(), 31.toUShort())

/* Coded index bits for int coded indices */
private val bitMask = arrayOf(1.toUInt(), 3.toUInt(), 7.toUInt(), 15.toUInt(), 31.toUInt())

/**
 * Not hardened for thread safety, and it's only designed to be used for a single WinMD file per instance... early days
 */
class WinMdNavigator(val quirks: Set<out NavigatorQuirk> = emptySet()) : Closeable {
    private lateinit var dosHeader: DosHeader
    private lateinit var ntHeader32: NTHeader32
    private lateinit var ntHeader64: NTHeader64
    private lateinit var byteBuffer: ByteBuffer
    private lateinit var stringTable: StreamRange
    private lateinit var blobTable: StreamRange
    private lateinit var guidTable: StreamRange
    private lateinit var tablesTable: StreamRange
    private lateinit var userStringsTable: StreamRange
    private val clrObjectTables: MutableMap<CLRMetadataType, NavigationTable> = EnumMap(CLRMetadataType::class.java)
    private val columnLayouts: MutableMap<CLRMetadataType, ColumnLayout> = EnumMap(CLRMetadataType::class.java)

    /* Hold full table scans  */
    private val reverseLookupSingulars: MutableMap<LookupTable, MutableMap<Any, Any?>> = ConcurrentHashMap()
    private val reverseLookupSingularRanges: MutableMap<LookupTable, MutableMap<IntRange, Int>> = ConcurrentHashMap()

    private var stringLen: Int = 0
    private var guidLen: Int = 0
    private var blobLen: Int = 0
    private lateinit var sectionsPointers: List<ReadRegion>

    private var randomAccessFile: RandomAccessFile? = null

    fun parseFile(path: Path) {
        if (!path.toFile().exists()) {
            throw IllegalArgumentException("Could not open file [${path}]")
        }

        this.randomAccessFile = RandomAccessFile(path.toString(), "r")
        try {
            val mappedByteBuffer =
                randomAccessFile!!.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile!!.length())
            // WinMd format is LITTLE ENDIAN based
            mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            parseContents(mappedByteBuffer)
        } catch (e: Exception) {
            randomAccessFile!!.close()
        }
    }

    fun parseContents(byteBuffer: MappedByteBuffer) {
        dosHeader = fillObject(byteBuffer, DosHeader::class)
        if (dosHeader.signature != 0x5A4D.toUShort()) {
            throw IllegalArgumentException("File Header doesn't match a well know WinMD format")
        }

        byteBuffer.position(dosHeader.lfanew.toInt())
        ntHeader32 = fillObject(byteBuffer, NTHeader32::class)

        if (ntHeader32.fileHeader.sectionCount == 0.toUShort() || ntHeader32.fileHeader.sectionCount > 100.toUShort()) {
            throw IllegalArgumentException("Invalid section count [${ntHeader32.fileHeader.sectionCount}]; Should be 0 < count <= 100")
        }

        val clrVirtualAddress: UInt
        val sectionCount: Int
        val sectionHeadPtr: Int
        when (ntHeader32.optionalHeader.magic) {
            PE_HEADER_MAGIC -> {
                if (ntHeader32.fileHeader.optionalHeaderLength != ntHeader32.optionalHeader.length().toUShort()) {
                    throw IllegalStateException(
                        "Optional Header should be ${ntHeader32.fileHeader.optionalHeaderLength} but is ${
                            ntHeader32.optionalHeader.length().toUShort()
                        }"
                    )
                }

                sectionsPointers = ntHeader32.optionalHeader.sectionPointers
                sectionHeadPtr = (dosHeader.lfanew + ntHeader32.length()).toInt()
                sectionCount = ntHeader32.fileHeader.sectionCount.toInt()
            }

            PE_PLUS_HEADER_MAGIC -> {
                // We're actually an extended header so read it into the header Again
                byteBuffer.position(dosHeader.lfanew.toInt())
                ntHeader64 = fillObject(byteBuffer, NTHeader64::class)
                if (ntHeader64.fileHeader.optionalHeaderLength != ntHeader64.optionalHeader.length().toUShort()) {
                    throw IllegalStateException(
                        "Optional Header should be ${ntHeader32.fileHeader.optionalHeaderLength} but is ${
                            ntHeader32.optionalHeader.length().toUShort()
                        }"
                    )
                }

                sectionsPointers = ntHeader64.optionalHeader.sectionPointers
                sectionHeadPtr = (dosHeader.lfanew + ntHeader64.length()).toInt()
                sectionCount = ntHeader64.fileHeader.sectionCount.toInt()
            }

            else -> throw IllegalStateException("Invalid optional header magic value")
        }

        clrVirtualAddress = sectionsPointers[SECTION_CLR].virtualAddress
        val clrSection = sectionFromRva(byteBuffer, sectionHeadPtr, sectionCount, clrVirtualAddress)
            ?: throw IllegalStateException("PE section containing CLI header not found")

        byteBuffer.position(offsetFromRva(clrSection, clrVirtualAddress))
        val cli = fillObject(byteBuffer, ImageCor20::class)
        if (cli.cb != cli.length()) {
            throw IllegalStateException("Invalid CLI header Length ${cli.cb} != ${cli.length()}")
        }

        val metadataSection = sectionFromRva(byteBuffer, sectionHeadPtr, sectionCount, cli.metadata.virtualAddress)
            ?: throw IllegalStateException("PE section containing CLI metadata not found")

        val metaDataPtr = offsetFromRva(metadataSection, cli.metadata.virtualAddress)
        byteBuffer.position(metaDataPtr)
        if (byteBuffer.getInt() != CLI_METADATA_MAGIC) {
            throw IllegalStateException("CLI metadata magic signature not found")
        }

        // Fetch Metadata version Length
        byteBuffer.position(metaDataPtr + 12)
        val versionLength = byteBuffer.getInt()

        // Fetch Metadata stream count
        byteBuffer.position(metaDataPtr + versionLength + 18)
        val streamCount = byteBuffer.getShort()

        // Iterator over the tables
        byteBuffer.position(metaDataPtr + versionLength + 20)

        repeat((0 until streamCount).count()) {
            val range = fillObject(byteBuffer, StreamRange::class)
            val stringStart = byteBuffer.position()
            val streamName = fillObject(byteBuffer, StreamName::class)
            when (streamName.toSafeString()) {
                STREAM_STRINGS -> stringTable = range.addOffset(metaDataPtr)
                STREAM_BLOB -> blobTable = range.addOffset(metaDataPtr)
                STREAM_GUID -> guidTable = range.addOffset(metaDataPtr)
                STREAM_TABLE -> tablesTable = range.addOffset(metaDataPtr)
                STREAM_USER_STRINGS -> userStringsTable = range.addOffset(metaDataPtr)
                else -> throw IllegalStateException("Unknown metadata stream [${streamName.toSafeString()}]")
            }

            // Padding between each stream definition
            byteBuffer.position(stringStart + streamName.paddedBytesUsed())
        }

        fillMajorTableIndexSizes(byteBuffer)
        preFillObjectTables(byteBuffer)

        this.byteBuffer = byteBuffer
    }

    private fun fillMajorTableIndexSizes(byteBuffer: ByteBuffer) {
        tablesTable.seek(byteBuffer, 6)
        val array = ByteArray(1).apply { byteBuffer.get(this) }
        val heapSizesBitSet = BitSet.valueOf(ByteBuffer.wrap(array))

        stringLen = if (heapSizesBitSet.get(0)) 4 else 2
        guidLen = if (heapSizesBitSet.get(1)) 4 else 2
        blobLen = if (heapSizesBitSet.get(2)) 4 else 2
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun preFillObjectTables(byteBuffer: ByteBuffer) {
        tablesTable.seek(byteBuffer, 8)
        val metadataTableBytes = ByteArray(8).apply { byteBuffer.get(this) }
        val metadataTableBits = BitSet.valueOf(metadataTableBytes)

        tablesTable.seek(byteBuffer, 24)

        val foundMdTypes = (0 until 64).mapNotNull { idx ->
            if (!metadataTableBits.get(idx)) {
                return@mapNotNull null
            }
            val rowCount = byteBuffer.getInt()
            val mdType = CLRMetadataType.fromIndex(idx) ?: throw IllegalStateException("Unknown metadata table $idx")
            clrObjectTables[mdType] = NavigationTable(mdType, rowCount)
            mdType
        }.toList()

        var clrTableRoot: Int = byteBuffer.position()

        /* The CLR table blocks are found immediately after the table count bits, so we replay each start to be offset from the last */
        foundMdTypes.mapNotNull { mdType -> clrObjectTables[mdType] }
            .forEach { table ->
                table.startIndex = clrTableRoot
                table.columnLayout = getColumnLayout(table.type)
                table.size = table.rowCount * table.columnLayout.rowWidth
                clrTableRoot += table.size
                println(
                    "Table Start: ${table.type.toString().padStart(25, ' ')} -- ${
                        table.startIndex.toHexString(kotlin.text.HexFormat.UpperCase)
                    } -- ${table.columnLayout.columnSizes}"
                )
            }
    }

    /** Identify the section containing the specified virtual address */
    private fun sectionFromRva(
        byteBuffer: MappedByteBuffer,
        sectionHeadPtr: Int,
        sectionCount: Int,
        virtualAddress: UInt
    ): SectionHeader? {
        byteBuffer.position(sectionHeadPtr)
        return (0 until sectionCount).firstNotNullOfOrNull { sectionNum ->
            val header = fillObject(byteBuffer, SectionHeader::class)
            if (virtualAddress >= header.virtualAddress && virtualAddress < header.virtualAddress + header.physicalAddressOrVirtualSize) {
                header
            } else {
                null
            }
        }
    }

    /** Lookup the absolute file location of the virtual address within the section */
    private fun offsetFromRva(section: SectionHeader, rva: UInt): Int {
        return (rva - section.virtualAddress + section.rawDataPtr).toInt()
    }

    fun getCount(type: CLRMetadataType): Int {
        return clrObjectTables[type]?.rowCount ?: 0
    }

    /**
     * Calculates the packed table column row index lengths
     */
    fun getColumnLayout(type: CLRMetadataType): ColumnLayout {
        return columnLayouts.computeIfAbsent(type) {
            val typeLengths = when (type) {
                MODULE -> arrayOf(2, stringLen, guidLen, guidLen, guidLen)
                TYPE_REF -> arrayOf(compositeIndexSize(ResolutionScope::class), stringLen, stringLen)
                TYPE_DEF -> arrayOf(
                    4,
                    stringLen,
                    stringLen,
                    compositeIndexSize(TypeDefOrRef::class),
                    indexSize(FIELD),
                    indexSize(METHOD_DEF)
                )

                FIELD -> arrayOf(2, stringLen, blobLen)
                METHOD_DEF -> arrayOf(4, 2, 2, stringLen, blobLen, indexSize(PARAM))
                PARAM -> arrayOf(2, 2, stringLen)
                INTERFACE_IMPL -> arrayOf(indexSize(TYPE_DEF), compositeIndexSize(TypeDefOrRef::class))
                MEMBER_REF -> arrayOf(compositeIndexSize(MemberRefParent::class), stringLen, blobLen)
                CONSTANT -> arrayOf(2, compositeIndexSize(HasConstant::class), blobLen)
                CUSTOM_ATTRIBUTE -> arrayOf(
                    compositeIndexSize(HasCustomAttribute::class),
                    compositeIndexSize(CustomAttributeType::class),
                    blobLen
                )

                FIELD_MARSHAL -> arrayOf(compositeIndexSize(HasFieldMarshal::class), blobLen)
                DECL_SECURITY -> arrayOf(2, compositeIndexSize(HasDeclSecurity::class), blobLen)
                CLASS_LAYOUT -> arrayOf(2, 4, indexSize(TYPE_DEF))
                FIELD_LAYOUT -> arrayOf(4, indexSize(FIELD))
                STAND_ALONE_SIG -> arrayOf(blobLen)
                EVENT_MAP -> arrayOf(indexSize(TYPE_DEF), indexSize(EVENT))
                EVENT -> arrayOf(2, stringLen, compositeIndexSize(TypeDefOrRef::class))
                PROPERTY_MAP -> arrayOf(indexSize(TYPE_DEF), indexSize(PROPERTY))
                PROPERTY -> arrayOf(2, stringLen, blobLen)
                METHOD_SEMANTICS -> arrayOf(2, indexSize(METHOD_DEF), compositeIndexSize(HasSemantics::class))
                METHOD_IMPL -> arrayOf(
                    indexSize(TYPE_DEF),
                    compositeIndexSize(MethodDefOrRef::class),
                    compositeIndexSize(MethodDefOrRef::class)
                )

                MODULE_REF -> arrayOf(stringLen)
                TYPE_SPEC -> arrayOf(blobLen)
                IMPL_MAP -> arrayOf(2, compositeIndexSize(MemberForwarded::class), stringLen, indexSize(MODULE_REF))
                FIELD_RVA -> arrayOf(4, indexSize(FIELD))
                ASSEMBLY -> arrayOf(4, 8, 4, blobLen, stringLen, stringLen)
                ASSEMBLY_PROCESSOR -> arrayOf(4)
                ASSEMBLY_OS -> arrayOf(4, 4, 4)
                ASSEMBLY_REF -> arrayOf(8, 4, blobLen, stringLen, stringLen, blobLen)
                ASSEMBLY_REF_PROCESSOR -> arrayOf(4, indexSize(ASSEMBLY_REF))
                ASSEMBLY_REF_OS -> arrayOf(4, 4, 4, indexSize(ASSEMBLY_REF))
                FILE -> arrayOf(4, stringLen, blobLen)
                EXPORTED_TYPE -> arrayOf(4, 4, stringLen, stringLen, compositeIndexSize(Implementation::class))
                MANIFEST_RESOURCE -> arrayOf(4, 4, stringLen, compositeIndexSize(Implementation::class))
                NESTED_CLASS -> arrayOf(indexSize(TYPE_DEF), indexSize(TYPE_DEF))
                GENERIC_PARAM -> arrayOf(2, 2, compositeIndexSize(TypeOrMethodDef::class), stringLen)
                METHOD_SPEC -> arrayOf(compositeIndexSize(MethodDefOrRef::class), blobLen)
                GENERIC_PARAM_CONSTRAINT -> arrayOf(indexSize(GENERIC_PARAM), compositeIndexSize(TypeDefOrRef::class))
            }
            ColumnLayout.from(typeLengths)
        }
    }

    private fun indexSize(type: CLRMetadataType): Int {
        return if (getCount(type) < (1 shl 16)) 2 else 4
    }

    /* Fetch the index size calculated for this PE/COFF files based on the net count of all references of this composite
       type */
    private fun <T : WinMdCompositeReference> compositeIndexSize(compositeInterface: KClass<T>): Int {
        // We throw an error if there's no Object type definition, which should be stamped on all composite reference
        // types to identify their original metadata type
        val prefixBits = compositeInterface.findAnnotation<InterfaceSpec>()!!.typePrefixBits
        val totalSum =
            compositeInterface.sealedSubclasses.maxOf { getCount(it.findAnnotation<ObjectType>()!!.objectType) }
        return if (totalSum < (1 shl (16 - prefixBits))) 2 else 4
    }

    fun getObjectTableOffset(type: CLRMetadataType, rowIndex: Int, columnIndex: Int): Int {
        val objectTable = clrObjectTables[type]
            ?: throw IllegalStateException("Table for [$type] doesn't exist in this file")
        val rowWidth = objectTable.columnLayout.rowWidth
        val columnOffset = objectTable.columnLayout.columnOffsets[columnIndex]
        return objectTable.startIndex + (rowIndex * rowWidth) + columnOffset
    }

    fun readFromTable(
        type: CLRMetadataType,
        rowIndex: Int,
        columnIndex: Int
    ): Any {
        val objectTable = clrObjectTables[type]
            ?: throw IllegalStateException("Table for [$type] doesn't exist in this file")
        val rowWidth = objectTable.columnLayout.rowWidth
        val columnOffset = objectTable.columnLayout.columnOffsets[columnIndex]
        val returnType = when (val colLen = objectTable.columnLayout.columnSizes[columnIndex]) {
            1 -> UByte::class
            2 -> UShort::class
            4 -> UInt::class
            8 -> ULong::class
            else -> throw IllegalStateException("Encoded length of table data type:[$type] column:[$columnIndex] is invalid ($colLen)")
        }

        objectTable.seek(byteBuffer, (rowIndex * rowWidth) + columnOffset)
        val tblVal = parsePrimitive(byteBuffer, returnType)
        return tblVal
    }

    fun readFromString(stringPtr: Any): String {
        stringTable.seek(byteBuffer, convertToInt(stringPtr))

        val bytes = ByteArray(1024)
        val sb = StringBuilder()

        outer@ while (true) {
            byteBuffer.get(bytes)

            for (b in bytes) {
                // TODO use a better string coder
                if (b == ZERO_BYTE) {
                    break@outer
                }
                sb.append(b.toChar())
            }
        }

        return sb.toString()
    }

    fun readFromGuid(guidPtr: Any): ByteArray {
        // Read the length into buff
        guidTable.seek(byteBuffer, convertToInt(guidPtr))
        val bytes = ByteArray(16)
        byteBuffer.get(bytes)
        return bytes
    }

    fun readFromBlob(blobPtr: Any): ByteArray {
        blobTable.seek(byteBuffer, convertToInt(blobPtr))

        // Calculate length of blob length field
        var iByte = byteBuffer.get()
        val iByteShr = (iByte.toInt() shr 5)
        val blobSizeBytes = if (iByteShr < 4) {
            iByte = iByte and 0x7f.toByte()
            1
        } else if (iByteShr < 6) {
            iByte = iByte and 0x3f.toByte()
            2
        } else if (iByteShr < 7) {
            iByte = iByte and 0x1f.toByte()
            4
        } else {
            throw IllegalStateException("Invalid blob encoding")
        }

        // Read the length into buff
        blobTable.seek(byteBuffer, convertToInt(blobPtr))
        val blobLengthBuff = ByteArray(blobSizeBytes)
        byteBuffer.get(blobLengthBuff)
        blobLengthBuff[0] = iByte // Correct against first byte extra bits for length sizing
        val length = when (blobSizeBytes) {
            1 -> ByteBuffer.wrap(blobLengthBuff).order(ByteOrder.LITTLE_ENDIAN).get().toInt()
            2 -> ByteBuffer.wrap(blobLengthBuff).order(ByteOrder.LITTLE_ENDIAN).getShort().toInt()
            4 -> ByteBuffer.wrap(blobLengthBuff).order(ByteOrder.LITTLE_ENDIAN).getInt()
            else -> throw IllegalStateException("Invalid blob encoding") // Can never happen
        }
        val bytes = ByteArray(length)
        byteBuffer.get(bytes)
        return bytes
    }

    fun reverseLookupRow(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        matchValue: Int,
    ): Int? {
        val resolvedLookupTable =
            reverseLookupSingulars.computeIfAbsent(
                LookupTable(
                    remoteType,
                    remoteColumnIndex
                )
            ) { lookupTable ->
                val startTime = System.nanoTime()
                val rowCount = getCount(remoteType)
                try {
                    val tableLookup = ConcurrentHashMap<Any, Any?>()
                    (0 until rowCount).forEach { row ->
                        tableLookup[convertToInt(
                            readFromTable(
                                lookupTable.type,
                                row, lookupTable.column
                            )
                        )] = row
                    }
                    tableLookup
                } finally {
                    val duration = System.nanoTime() - startTime
                    println("ReverseLookup Cache [$remoteType/$remoteColumnIndex] took ${duration / 1000000.0}ms")
                }
            }

        return resolvedLookupTable[matchValue]?.toString()?.toInt() ?: return null
    }

    fun reverseLookupRangeRow(
        referenceType: CLRMetadataType,
        parentType: CLRMetadataType,
        parentColumnIndex: Int,
        targetRow: Int,
    ): Int? {
        return reverseLookupSingularRanges.computeIfAbsent(
            LookupTable(
                parentType,
                parentColumnIndex
            )
        ) { lookupTable ->
            val startTime = System.nanoTime()
            val rowCount = getCount(parentType)
            val targetRowCount = getCount(referenceType)
            try {
                val tableLookup = ConcurrentHashMap<IntRange, Int>()

                var currentRangeHeadPtr = -1
                var currentRangeParentRow = -1

                (0 until rowCount).forEach { row ->
                    val ptr = readFromTable(
                        lookupTable.type,
                        row, lookupTable.column
                    ).toString().toInt()

                    if (ptr in 1..targetRowCount) {
                        if (currentRangeParentRow == -1) {
                            currentRangeParentRow = row + 1
                            currentRangeHeadPtr = ptr
                        } else if (currentRangeHeadPtr != ptr) {
                            tableLookup[IntRange(currentRangeHeadPtr, ptr - 1)] = currentRangeParentRow
                            currentRangeParentRow = row + 1
                            currentRangeHeadPtr = ptr
                        }
                    } else {
                        println("Fault accumulating reverseLookupRangeRow to ${lookupTable.type}/$ptr")
                    }
                }

                // Finish of the last range when we hit the bottom of the list
                tableLookup[IntRange(currentRangeHeadPtr, targetRowCount)] = currentRangeParentRow

                tableLookup
            } finally {
                val duration = System.nanoTime() - startTime
                println("LookupRange Cache [$parentType/$parentColumnIndex] took ${duration / 1000000.0}ms")
            }
        }.entries.firstOrNull { it.key.contains(targetRow) }?.value
    }

    fun calculateInterfacePtr(
        interfaceClazz: KClass<out WinMdCompositeReference>,
        pointer: Any
    ): Pair<KClass<out WinMdObject>, Int>? {
        val interfaceSpec = interfaceClazz.findAnnotation<InterfaceSpec>()!!
        val typePrefixBits = interfaceSpec.typePrefixBits
        val typePrefixBitClassOrder = interfaceSpec.classOrder
        // TODO can be done a lot more efficiently by pre-allocating a lot of these structs
        val compatibleTypes = interfaceClazz.sealedSubclasses.map { it.findAnnotation<ObjectType>()!!.objectType to it }
        val targetTableClazz: KClass<out WinMdObject>?
        when (pointer) {
            is UByte -> throw IllegalStateException("Bytes cannot hold pointer references")
            is UShort -> {
                val rowNum = (pointer.toInt() shr typePrefixBits)
                return if (rowNum == 0) {
                    null
                } else {
                    val tableBitset = (pointer and bitMaskShort[typePrefixBits - 1]).toInt()
                    val targetTableClazz =
                        compatibleTypes.firstOrNull { it.second.simpleName == typePrefixBitClassOrder[tableBitset] }?.second?.let { it as KClass<out WinMdObject> }
                            ?: return null
                    return targetTableClazz to rowNum
                }
            }

            is UInt -> {
                val rowNum = (pointer.toInt() shr typePrefixBits)
                if (rowNum == 0) {
                    return null
                } else {
                    val tableBitset = (pointer and bitMask[typePrefixBits - 1]).toInt()
                    val targetTableClazz =
                        compatibleTypes.firstOrNull { it.second.simpleName == typePrefixBitClassOrder[tableBitset] }?.second?.let { it as KClass<out WinMdObject> }
                            ?: return null
                    return targetTableClazz to rowNum
                }
            }

            is ULong -> throw IllegalStateException("Longs cannot hold pointer references")

            else -> throw IllegalStateException("${pointer::class.simpleName} cannot hold pointer references")
        }
    }

    fun isValidPtr(targetClazz: KClass<out WinMdCompositeReference>, pointer: Any): Boolean {
        return calculateInterfacePtr(targetClazz, pointer) != null
    }

    override fun close() {
        randomAccessFile?.close()
    }
}

private fun bitsNeeded(vae: Int): Byte {
    var value = vae
    value--

    var bits = ONE_BYTE

    do {
        value = value shr 1
        if (value < ONE_BYTE) {
            break
        }
        ++bits
    } while (true)

    return bits
}

data class LookupTable(val type: CLRMetadataType, val column: Int)
