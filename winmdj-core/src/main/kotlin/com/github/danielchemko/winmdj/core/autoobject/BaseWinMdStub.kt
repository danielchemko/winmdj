package com.github.danielchemko.winmdj.core.autoobject

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.autoobject.model.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.*
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

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
                // TODO double check correct endianness and improve perf
                is UShort -> byteArrayOf(
                    (tableValue.toInt() shr 8).toByte(),
                    (tableValue and 255u).toByte()
                )

                // TODO double check correct endianness and improve perf
                is UInt -> byteArrayOf(
                    (tableValue.toInt() shr 24).toByte(),
                    (tableValue.toInt() shr 16).toByte(),
                    (tableValue.toInt() shr 8).toByte(),
                    (tableValue and 255u).toByte()
                )

                // TODO double check correct endianness and improve perf
                is ULong -> byteArrayOf(
                    (tableValue.toInt() shr 56).toByte(),
                    (tableValue.toInt() shr 48).toByte(),
                    (tableValue.toInt() shr 40).toByte(),
                    (tableValue.toInt() shr 32).toByte(),
                    (tableValue.toInt() shr 24).toByte(),
                    (tableValue.toInt() shr 16).toByte(),
                    (tableValue.toInt() shr 8).toByte(),
                    (tableValue and 255u).toByte()
                )

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

    private val bitMaskShort = arrayOf(
        1.toUShort(),
        1.toUShort(),
        2.toUShort(),
        2.toUShort(),
        3.toUShort(),
        3.toUShort(),
        4.toUShort(),
        4.toUShort(),
        5.toUShort(),
        5.toUShort()
    )
    private val bitMask = arrayOf(
        1.toUInt(),
        1.toUInt(),
        2.toUInt(),
        2.toUInt(),
        3.toUInt(),
        3.toUInt(),
        4.toUInt(),
        4.toUInt(),
        5.toUInt(),
        5.toUInt()
    )

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
        val targetTableClazz: KClass<out WinMdObject>
        val rowNum: Int
        when (val fieldPointer = getObjectTableValue(type, columnIndex)) {
            is UByte -> throw IllegalStateException("Bytes cannot hold pointer references")
            is UShort -> {
                rowNum = (fieldPointer.toInt() shr typePrefixBits)
                if(rowNum == 0) {
                    return null
                }
                val tableBitset = (fieldPointer and bitMaskShort[typePrefixBits]).toInt()
                targetTableClazz =
                    compatibleTypes.first { it.second.simpleName == typePrefixBitClassOrder[tableBitset] }.second as KClass<out WinMdObject>
            }

            is UInt -> {
                rowNum = (fieldPointer.toInt() shr typePrefixBits)
                if(rowNum == 0) {
                    return null
                }
                val tableBitset = (fieldPointer and bitMask[typePrefixBits]).toInt()
                targetTableClazz =
                    compatibleTypes.first { it.second.simpleName == typePrefixBitClassOrder[tableBitset] }.second as KClass<out WinMdObject>
            }

            is ULong -> throw IllegalStateException("Longs cannot hold pointer references")

            else -> throw IllegalStateException("${fieldPointer::class.simpleName} cannot hold pointer references")
        }

        return getObjectMapper().getCursor(targetTableClazz.java).get(rowNum) as T
    }

    override fun <T : WinMdObject> lookupConcreteReferent(
        type: CLRMetadataType,
        columnIndex: Int,
        targetTableClazz: KClass<T>
    ): T? {
        val rowNumber = getObjectTableValue(type, columnIndex)
        if(rowNumber == 0) {
            return null
        }
        return getObjectMapper().getCursor(targetTableClazz.java).get(rowNumber.toString().toInt())
    }


    /* TODO cached search first run?? */
    override fun <T : Any> getReverseReferentSingle(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        kClass: KClass<T>,
        matchValue: Any
    ): T? {
        TODO("Not yet implemented")
    }

    /* TODO cached search first run?? */
    override fun <T : Any> getReverseReferentPlural(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        kClass: KClass<T>,
        matchValue: Any
    ): List<T> {
        TODO("Not yet implemented")
    }
}