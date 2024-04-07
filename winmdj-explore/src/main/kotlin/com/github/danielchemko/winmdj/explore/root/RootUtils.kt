package com.github.danielchemko.winmdj.explore.root

import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.WinMdCompositeReference
import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import com.github.danielchemko.winmdj.core.mdspec.getObjectType
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

@OptIn(ExperimentalStdlibApi::class)
fun toJumpPtrString(
    navigator: WinMdNavigator,
    valueType: CLRMetadataType,
    value: WinMdObject,
    column: Int,
    targetValueType: CLRMetadataType?,
    targetClazz: KClass<*>,
): String {
    val tableCellValue = value.getStub().getObjectTableValue(valueType, column)
    if (WinMdObject::class.isSuperclassOf(targetClazz)) {
        val rowVal = tableCellValue.toString().toInt()
        if (rowVal < 1 || rowVal > navigator.getCount(targetValueType!!)) {
            return ""
        }
        return (rowVal.toUInt() or ((valueType.bitSetIndex shl 24).toUInt())).toHexString(HexFormat.UpperCase)
    } else if (WinMdCompositeReference::class.isSuperclassOf(targetClazz)) {
        val tableCellValue = value.getStub().getObjectTableValue(valueType, column)
        val calcd =
            navigator.calculateInterfacePtr(targetClazz as KClass<out WinMdCompositeReference>, tableCellValue)
                ?: return ""
        return (calcd.second.toUInt() or (getObjectType(calcd.first).objectType.bitSetIndex shl 24).toUInt()).toHexString(
            HexFormat.UpperCase
        )
    }
    return ""
}