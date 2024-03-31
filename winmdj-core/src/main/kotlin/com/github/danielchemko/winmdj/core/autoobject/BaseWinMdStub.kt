package com.github.danielchemko.winmdj.core.autoobject

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.*
import com.github.danielchemko.winmdj.parser.LookupTable
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import com.github.danielchemko.winmdj.util.convertToInt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

/* Coded index bits for short coded indices */
private val bitMaskShort = arrayOf(1.toUShort(), 3.toUShort(), 7.toUShort(), 15.toUShort(), 31.toUShort())

/* Coded index bits for int coded indices */
private val bitMask = arrayOf(1.toUInt(), 3.toUInt(), 7.toUInt(), 15.toUInt(), 31.toUInt())

@Suppress("UNCHECKED_CAST") // We evaluate that its always T or fail
class BaseWinMdStub(
    private val objectMapper: MdObjectMapper,
    private val navigator: WinMdNavigator,
    private var index: Int = 1
) : WinMdStub {

    override fun getObjectMapper(): MdObjectMapper {
        return objectMapper
    }

    override fun getNavigator(): WinMdNavigator {
        return navigator
    }

    override fun getToken(type: CLRMetadataType): UInt {
        val prefix = type.bitSetIndex shl 24
        return (prefix + index).toUInt()
    }

    override fun setRowNumberIndex(index: Int) {
        assert(index > 0)
        this.index = index
    }

    override fun getRowNumber(): Int {
        return index
    }

    override fun <T : Any> lookupTableValue(type: CLRMetadataType, columnIndex: Int, responseType: KClass<T>?): T {
        val rawTableResult = getObjectTableValue(type, columnIndex)

        if (responseType == null || responseType.isInstance(rawTableResult)) {
            return rawTableResult as T
        } else {
            throw IllegalStateException("Parser inconsistency reading column value. type:[$type] expected:[$responseType] actual:[${rawTableResult::class}]")
        }
    }

    override fun lookupString(type: CLRMetadataType, columnIndex: Int): String {
        val tableValue = getObjectTableValue(type, columnIndex)
        return navigator.readFromString(tableValue)
    }

    override fun lookupBlob(type: CLRMetadataType, columnIndex: Int): ByteArray {
        val tableValue = getObjectTableValue(type, columnIndex)
        return navigator.readFromBlob(tableValue)
    }

    override fun lookupGuid(type: CLRMetadataType, columnIndex: Int): ByteArray {
        val tableValue = getObjectTableValue(type, columnIndex)
        return navigator.readFromGuid(tableValue)
    }

    override fun <T : ValueEnum<*, *>> lookupBitsetEnum(
        type: CLRMetadataType,
        columnIndex: Int,
        enumClazz: KClass<T>
    ): List<T> {
        val tableValue = getObjectTableValue(type, columnIndex)
        val candidates = enumClazz.java.enumConstants
        // TODO need to AND???
        return candidates.filter { it.getCode() == tableValue }.toList()
    }

    override fun <T : WinMdObject> lookupList(
        type: CLRMetadataType,
        columnIndex: Int,
        subOrdinal: Int,
        childListTerminator: Int,
        childClazz: KClass<T>
    ): List<T> {
        val childJClass = childClazz.java
        // TODO cache
        val targetType = childClazz.findAnnotation<ObjectType>()!!.objectType
        var row = getObjectTableValue(type, columnIndex).toString().toInt()
        val maxIndex = navigator.getCount(targetType)

        if (row > maxIndex) {
            println("OutOfBoundsPtr detected: $type/${getRowNumber()}/$columnIndex -> $targetType/$row > $maxIndex")
            return emptyList()
        }

        val childTableSequential = childListTerminator == CHILD_LIST_TERMINATOR_ASCENDING

        var oldSignatureField: Long = if (childTableSequential) {
            -1
        } else {
            getRandomObjectTableValue(targetType, row, subOrdinal).toString().toLong()
        }
        val list = mutableListOf<T>()
        do {
            val newSignatureField = getRandomObjectTableValue(targetType, row, subOrdinal).toString().toLong()
            if (childTableSequential) {
                if (newSignatureField > oldSignatureField) {
                    list.add(getObjectMapper().getCursor(childJClass).get(row++))
                    oldSignatureField = newSignatureField
                } else {
                    break
                }
            } else {
                if (newSignatureField == oldSignatureField) {
                    list.add(getObjectMapper().getCursor(childJClass).get(row++))
                } else {
                    break
                }
            }
        } while (row <= maxIndex)
        return list
    }

    override fun lookupBitset(
        type: CLRMetadataType,
        columnIndex: Int
    ): BitSet {
        val tableValue = getObjectTableValue(type, columnIndex)

        return BitSet.valueOf(
            when (tableValue) {
                is UByte -> byteArrayOf(tableValue.toByte())
                is UShort -> ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(tableValue.toShort())
                    .array()
//                    byteArrayOf(
//                    (tableValue.toInt() shr 8).toByte(),
//                    (tableValue and 255u).toByte()
//                )

                is UInt -> ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(tableValue.toInt()).array()
//                    byteArrayOf(
//                    (tableValue.toInt() shr 24).toByte(),
//                    (tableValue.toInt() shr 16).toByte(),
//                    (tableValue.toInt() shr 8).toByte(),
//                    (tableValue and 255u).toByte()
//                )

                is ULong -> ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(tableValue.toLong()).array()
//                byteArrayOf(
//                    (tableValue.toInt() shr 56).toByte(),
//                    (tableValue.toInt() shr 48).toByte(),
//                    (tableValue.toInt() shr 40).toByte(),
//                    (tableValue.toInt() shr 32).toByte(),
//                    (tableValue.toInt() shr 24).toByte(),
//                    (tableValue.toInt() shr 16).toByte(),
//                    (tableValue.toInt() shr 8).toByte(),
//                    (tableValue and 255u).toByte()
//                )

                else -> throw IllegalStateException("$tableValue cannot be converted into a Bitset")
            }
        )
    }

    override fun getObjectTableValue(type: CLRMetadataType, columnIndex: Int): Any {
        if (index > navigator.getCount(type)) {
            throw IllegalArgumentException("Invalid row index")
        }

        return navigator.readFromTable(type, index - 1, columnIndex)
    }

    override fun getRandomObjectTableValue(type: CLRMetadataType, row: Int, columnIndex: Int): Any {
        if (row > navigator.getCount(type)) {
            throw IllegalArgumentException("Invalid row index")
        }

        return navigator.readFromTable(type, row - 1, columnIndex)
    }

    inline fun <reified T : Enum<R>, reified V, reified R : Any> convertValueToEnum(
        enumClazz: KClass<R>,
        value: V
    ): R {
        val method = enumClazz.java.methods.firstOrNull { it.name == "getCode" }
        if (method == null) {
            throw IllegalStateException("Unable to identity Enum [${enumClazz}] code field")
        }
        return enumClazz.java.enumConstants.firstOrNull { method.invoke(it) == value }
            ?: throw IllegalStateException("Enum [${enumClazz.java.simpleName}] doesn't contain code [$value]")
    }

    override fun getObjectTableOffset(type: CLRMetadataType, columnIndex: Int): Int {
        return navigator.getObjectTableOffset(type, index - 1, columnIndex)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T : WinMdCompositeReference> lookupInterfaceReferent(
        type: CLRMetadataType,
        columnIndex: Int,
        interfaceClazz: KClass<T>
    ): T? {
        val interfaceSpec = interfaceClazz.findAnnotation<InterfaceSpec>()!!
        val typePrefixBits = interfaceSpec.typePrefixBits
        val typePrefixBitClassOrder = interfaceSpec.classOrder
        // TODO can be done a lot more efficiently by pre-allocating a lot of these structs
        val compatibleTypes = interfaceClazz.sealedSubclasses.map { it.findAnnotation<ObjectType>()!!.objectType to it }
        val targetTableClazz: KClass<out WinMdObject>?
        val rowNum: Int
        when (val fieldPointer = getObjectTableValue(type, columnIndex)) {
            is UByte -> throw IllegalStateException("Bytes cannot hold pointer references")
            is UShort -> {
                rowNum = (fieldPointer.toInt() shr typePrefixBits)
                if (rowNum == 0) {
                    return null
                }
                val tableBitset = (fieldPointer and bitMaskShort[typePrefixBits - 1]).toInt()
                targetTableClazz =
                    compatibleTypes.firstOrNull { it.second.simpleName == typePrefixBitClassOrder[tableBitset] }?.second as KClass<out WinMdObject>?
            }

            is UInt -> {
                rowNum = (fieldPointer.toInt() shr typePrefixBits)
                if (rowNum == 0) {
                    return null
                }
                val tableBitset = (fieldPointer and bitMask[typePrefixBits - 1]).toInt()
                targetTableClazz =
                    compatibleTypes.firstOrNull { it.second.simpleName == typePrefixBitClassOrder[tableBitset] }?.second as KClass<out WinMdObject>?
            }

            is ULong -> throw IllegalStateException("Longs cannot hold pointer references")

            else -> throw IllegalStateException("${fieldPointer::class.simpleName} cannot hold pointer references")
        }

        if (targetTableClazz == null) {
            val v = getObjectTableValue(type, columnIndex)
            throw IllegalStateException(
                "Cannot find table class type for pointer: $v (${
                    v.toString().toUInt().toHexString(kotlin.text.HexFormat.UpperCase)
                })"
            )
        }

        return getObjectMapper().getCursor(targetTableClazz.java).get(rowNum) as T
    }

    override fun <T : WinMdObject> lookupConcreteReferent(
        type: CLRMetadataType,
        columnIndex: Int,
        targetTableClazz: KClass<T>
    ): T? {
        val rowNumber = getObjectTableValue(type, columnIndex)
        if (rowNumber == 0) {
            return null
        }
        return getObjectMapper().getCursor(targetTableClazz.java).get(rowNumber.toString().toInt())
    }

    private fun getChildListHeadRow(
        originType: CLRMetadataType,
        sourceRow: Int,
        column: Int,
        childListTerminator: Int
    ): Int {
        if (childListTerminator == CHILD_LIST_TERMINATOR_REPEATING) {
            val firstRowValue = getRandomObjectTableValue(originType, sourceRow, column)
            var rowRef = sourceRow
            while (rowRef > 0) {
                if (getRandomObjectTableValue(originType, rowRef, column) != firstRowValue) {
                    return rowRef + 1
                } else {
                    rowRef--
                }
            }
            return 1 // We're at the top of the table, and we never found a non-matching result
        } else if (childListTerminator == CHILD_LIST_TERMINATOR_ASCENDING) {
            var lastRowValue = getRandomObjectTableValue(originType, sourceRow, column).toString().toInt()
            var rowRef = sourceRow
            while (rowRef > 0) {
                val currRowValue = getRandomObjectTableValue(originType, rowRef, column).toString().toInt()
                if (currRowValue >= lastRowValue) {
                    return rowRef + 1
                } else {
                    lastRowValue = currRowValue
                    rowRef--
                }
            }
            return 1 // We're at the top of the table, and we never found a higher integer
        } else {
            throw IllegalStateException("Child list terminator $childListTerminator not supported")
        }
    }

    private fun calculateReversePtr(
        returnType: KClass<out WinMdObject>,
        interfaceImplClazz: KClass<out WinMdCompositeReference>,
        reverseRowTarget: Int
    ): Int {
        val spec = interfaceImplClazz.findAnnotation<InterfaceSpec>()!!
        val tablePtr = spec.classOrder.indexOfFirst { it == returnType.simpleName }
        return ((reverseRowTarget.toUInt().toULong() shl spec.typePrefixBits).toInt() or tablePtr)
    }

    /**
     * Pretty overloaded function depending on the return type being requested, which ultimately dictates the strategy
     * to solve this relationship
     *
     * 1. RETURN TYPE is Singular (class); Ordinal should be > 0
     * 2. RETURN TYPE is Plural (class); Ordinal should be > 0
     * 3. RETURN TYPE is Singular (Interface); Ordinal determined by the first function on remote which contains this matching return type
     * 4. RETURN TYPE is Plural (Interface); Ordinal determined by the first function on remote which contains this matching return type
     */
    override fun <T : Any> computeReverseLookup(
        originType: CLRMetadataType,
        originClass: KClass<*>,
        ordinal: Int,
        subOrdinal: Int,
        childListTerminator: Int,
        returnType: KClass<T>,
        returnTypeIsList: Boolean,
    ): T? {
        if (returnTypeIsList) {
            if (WinMdObject::class.isSuperclassOf(returnType)) {
                val destinationType = returnType.findAnnotation<ObjectType>()!!.objectType

                val selfToken = getRowNumber()
                val foreignCursor = getObjectMapper().getCursor(returnType.java as Class<out WinMdObject>)

                val max = getNavigator().getCount(destinationType)
                return (1..max)
                    .filter { row -> getRandomObjectTableValue(destinationType, row, ordinal) == selfToken }
                    .map { row -> foreignCursor.get(row) }.toList() as T
            } else {
                // Interface type matching
                TODO()
            }
        } else {
            //TODO determine if this is a child reference or a child list head... only way currently is subOrdinal > -1???
            val reverseRowTarget: Int
            if (subOrdinal > -1) {
                reverseRowTarget = getChildListHeadRow(originType, getRowNumber(), subOrdinal, childListTerminator)
                if (reverseRowTarget == 0) {
                    return null
                }
            } else {
                reverseRowTarget = getRowNumber()
            }

            if (WinMdObject::class.isSuperclassOf(returnType)) {
                val destinationType = returnType.findAnnotation<ObjectType>()!!.objectType
                return getReverseReferentSingle(destinationType, ordinal, returnType, reverseRowTarget)
            } else {
                returnType.sealedSubclasses.forEach { interfaceImplClazz ->
                    val reverseRowTargetPtr = calculateReversePtr(
                        originClass as KClass<out WinMdObject>,
                        returnType as KClass<out WinMdCompositeReference>,
                        reverseRowTarget
                    )
                    val candidateType = interfaceImplClazz.findAnnotation<ObjectType>()!!.objectType
                    getReverseReferentSingle(
                        candidateType,
                        ordinal,
                        returnType,
                        reverseRowTargetPtr
                    )?.let { return it as T }
                }
                return null // No Interfaces have the row target
            }
        }
    }

    private fun <T : Any> getReverseReferentSingle(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        kClass: KClass<T>,
        matchValue: Any
    ): T? {
        val rowCount = navigator.getCount(remoteType)
        val matchValueInt = matchValue.toString().toInt()

        val resolvedLookupTable =
            navigator.reverseLookupSingulars.computeIfAbsent(
                LookupTable(
                    remoteType,
                    remoteColumnIndex
                )
            ) { lookupTable ->
                val startTime = System.nanoTime()
                try {
                    val tableLookup = ConcurrentHashMap<Any, Any?>()
                    (1..rowCount).forEach { row ->
                        tableLookup[convertToInt(
                            getRandomObjectTableValue(
                                lookupTable.type,
                                row,
                                lookupTable.column
                            )
                        )] = row
                    }
                    tableLookup
                } finally {
                    val duration = System.nanoTime() - startTime
                    println("Cache build [$remoteType/$remoteColumnIndex] took ${duration / 1000000.0}ms")
                }
            }

        val foundRow = resolvedLookupTable[matchValueInt]?.toString()?.toInt() ?: return null

        return getObjectMapper().getCursor(kClass.java as Class<out WinMdObject>).get(foundRow) as T
    }
}