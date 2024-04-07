package com.github.danielchemko.winmdj.core.autoobject

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.*
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
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

                is UInt -> ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(tableValue.toInt()).array()
                is ULong -> ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(tableValue.toLong()).array()
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

        //
        // TODO most of this moved into navigator for reuse
        //
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

//    private fun getChildListHeadRow(
//        originType: CLRMetadataType,
//        sourceRow: Int,
//        column: Int,
//        childListTerminator: Int
//    ): Int {
//        if (childListTerminator == CHILD_LIST_TERMINATOR_REPEATING) {
//            val firstRowValue = getRandomObjectTableValue(originType, sourceRow, column)
//            var rowRef = sourceRow - 1
//            while (rowRef > 0) {
//                if (getRandomObjectTableValue(originType, rowRef, column) != firstRowValue) {
//                    return rowRef + 1
//                } else {
//                    rowRef--
//                }
//            }
//            return 1 // We're at the top of the table, and we never found a non-matching result
//        } else if (childListTerminator == CHILD_LIST_TERMINATOR_ASCENDING) {
//            var lastRowValue = getRandomObjectTableValue(originType, sourceRow, column).toString().toInt()
//            var rowRef = sourceRow
//            while (rowRef > 0) {
//                val currRowValue = getRandomObjectTableValue(originType, rowRef, column).toString().toInt()
//                if (currRowValue >= lastRowValue) {
//                    return rowRef + 1
//                } else {
//                    lastRowValue = currRowValue
//                    rowRef--
//                }
//            }
//            return 1 // We're at the top of the table, and we never found a higher integer
//        } else if (childListTerminator == CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL) {
//            TODO()
//        } else {
//            throw IllegalStateException("Child list terminator $childListTerminator not supported")
//        }
//    }

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
    override fun computeReverseLookup(
        originClass: KClass<out WinMdObject>,
        returnClassColumn: Int,
        returnBaseClass: KClass<*>,
        returnTypeIsList: Boolean,
    ): Any? {
        // Because we're a reverse relationship, we fetch the direct pointer metadata from the class defining it, and
        // solve their problem in reverse

        val forwardPtrColumnInfo = getColumnInfo(returnBaseClass, returnClassColumn)
        val forwardPtrReturnClass = getColumnClassType(returnBaseClass, returnClassColumn)!!
        val forwardPtrColumnBase = getColumnBaseClassType(returnBaseClass, returnClassColumn)
        val forwardPtrBaseIsWinMd = WinMdObject::class.isSuperclassOf(forwardPtrColumnBase)

        // Determine if the expected return data
        val returnClassBaseIsWinMd = if (WinMdObject::class.isSuperclassOf(returnBaseClass)) {
            true
        } else if (WinMdCompositeReference::class.isSuperclassOf(returnBaseClass)) {
            false
        } else {
            throw IllegalStateException("Unsolvable class $returnBaseClass")
        }

        val targetRowNumber = getRowNumber()

        when (forwardPtrColumnInfo.table) {
            LookupType.TARGET_LIST -> {
                // The Singular -> Plural
                when (forwardPtrColumnInfo.childListTerminator) {
                    CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL -> {
                        return if (forwardPtrBaseIsWinMd) {
                            val originType = getObjectType(originClass).objectType
                            val type = getObjectType(returnBaseClass as KClass<out WinMdObject>).objectType
                            val rowNum =
                                navigator.reverseLookupRangeRow(originType, type, returnClassColumn, targetRowNumber)
                                    ?: return null
                            return objectMapper.getCursor(returnBaseClass.java).get(rowNum)
                        } else {
                            (returnBaseClass as KClass<out WinMdCompositeReference>).sealedSubclasses.firstNotNullOfOrNull { ifaceImplClazz ->
                                TODO()
//                                val type = getObjectType(ifaceImplClazz as KClass<out WinMdObject>).objectType
//                                val rowNum = navigator.reverseLookupRangeRow(type, returnClassColumn, targetRowNumber) ?: return null
//                                return objectMapper.getCursor(returnBaseClass.java).get(rowNum)
                            }
                        }
                    }

                    CHILD_LIST_TERMINATOR_REPEATING -> {
                        TODO()
                    }

                    CHILD_LIST_TERMINATOR_ASCENDING -> {
                        TODO()
                    }

                    else -> throw IllegalStateException("Unable to solve reverse target list when we don't have a child terminator strategy")
                }
            }

            LookupType.TARGET -> {
                return if (returnTypeIsList) {
                    if (returnClassBaseIsWinMd) {
                        val returnType = getObjectType(returnBaseClass as KClass<out WinMdObject>).objectType
                        objectMapper.getCursor(returnBaseClass.java).map { it }
                            .filter { stub ->
                                val objectPtr = stub.getStub().getObjectTableValue(returnType, returnClassColumn)
                                isStubMatches(
                                    originClass,
                                    forwardPtrReturnClass,
                                    returnClassColumn,
                                    forwardPtrBaseIsWinMd,
                                    targetRowNumber,
                                    objectPtr
                                )
                            }
                    } else {
                        objectMapper.getInterfaceCursor(returnBaseClass.java as Class<out WinMdCompositeReference>)
                            .map { it }.filter { stub ->
                                val returnType = getObjectType(stub::class).objectType
                                val objectPtr = stub.getStub().getObjectTableValue(returnType, returnClassColumn)
                                isStubMatches(
                                    originClass,
                                    forwardPtrReturnClass,
                                    returnClassColumn,
                                    forwardPtrBaseIsWinMd,
                                    targetRowNumber,
                                    objectPtr
                                )
                            }
                    }.toList()
                } else {
                    if (forwardPtrBaseIsWinMd) {
                        val type = getObjectType(returnBaseClass as KClass<out WinMdObject>).objectType
                        val rowNum = navigator.reverseLookupRow(type, returnClassColumn, targetRowNumber)
                        return if (rowNum == null || rowNum < 0) {
                            null
                        } else {
                            objectMapper.getCursor(returnBaseClass.java).get(rowNum + 1)
                        }
                    } else {
                        return (forwardPtrReturnClass as KClass<out WinMdCompositeReference>).sealedSubclasses.firstNotNullOfOrNull { ifaceImplClazz ->
                            val type = getObjectType(ifaceImplClazz as KClass<out WinMdObject>).objectType
                            val rowNum = navigator.reverseLookupRow(type, returnClassColumn, targetRowNumber)
                            if (rowNum == null || rowNum < 0) {
                                null
                            } else {
                                objectMapper.getCursor(ifaceImplClazz.java as Class<out WinMdObject>)
                                    .get(rowNum + 1) as Any?
                            }
                        }
                    }
                }
            }

            else -> {
                throw IllegalStateException("Unable to solve reverse lookup when the reverse isn't a TARGET_LIST or TARGET")
            }
        }
    }

    private fun isStubMatches(
        originClass: KClass<*>,
        returnBaseClass: KClass<*>,
        returnClassColumn: Int,
        forwardPtrBaseIsWinMd: Boolean,
        targetRowNumber: Int,
        objectPtr: Any,
    ): Boolean {
        return if (forwardPtrBaseIsWinMd) {
            objectPtr == targetRowNumber
        } else {
            val typeAndRow =
                navigator.calculateInterfacePtr(returnBaseClass as KClass<out WinMdCompositeReference>, objectPtr)
            if (typeAndRow == null) {
                false
            } else {
                originClass == typeAndRow.first && typeAndRow.second == targetRowNumber
            }
        }
    }

    private fun <T : Any> getReverseReferentSingle(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        kClass: KClass<T>,
        matchValue: Any
    ): T? {
        val matchValueInt = matchValue.toString().toInt()
        val foundRow = navigator.reverseLookupRow(remoteType, remoteColumnIndex, matchValueInt) ?: return null
        return getObjectMapper().getCursor(kClass.java as Class<out WinMdObject>).get(foundRow) as T
    }
}