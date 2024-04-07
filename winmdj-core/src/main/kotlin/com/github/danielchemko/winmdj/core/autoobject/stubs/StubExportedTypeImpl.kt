package com.github.danielchemko.winmdj.core.autoobject.stubs


import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.autoobject.BaseWinMdStub
import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.ExportedType
import com.github.danielchemko.winmdj.core.mdspec.WinMdStub
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import javax.annotation.processing.Generated

/**
 * THIS FILE IS AUTOMATICALLY GENERATED BY RegenerateSubs.kt. DO NOT EDIT IT BY HAND
 */

@Generated
class StubExportedTypeImpl(
    objectMapper: MdObjectMapper,
    navigator: WinMdNavigator,
    index: Int
) : ExportedType {

    val stub = BaseWinMdStub(objectMapper, navigator, index)

    override fun toString(): String {
        return "ExportedType/${getToken()}"
    }

    override fun getStub(): WinMdStub {
        return stub
    }

    override fun getRowNumber(): Int {
        return getStub().getRowNumber()
    }

    override fun getToken(): UInt {
        return stub.getToken(CLRMetadataType.EXPORTED_TYPE)
    }

    override fun getOffset(): UInt {
        return stub.getObjectTableOffset(CLRMetadataType.EXPORTED_TYPE, 0).toUInt()
    }

    override fun copy(rowNum: Int): StubExportedTypeImpl {
        return StubExportedTypeImpl::class.constructors.first()
            .call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
    }

    override fun getAttributes(): java.util.BitSet {
        return stub.lookupBitset(CLRMetadataType.EXPORTED_TYPE, 0)
    }

    override fun getImplementation(): com.github.danielchemko.winmdj.core.mdspec.Implementation? {
        return stub.lookupInterfaceReferent(
            CLRMetadataType.EXPORTED_TYPE,
            4,
            com.github.danielchemko.winmdj.core.mdspec.Implementation::class
        )
    }

    override fun getTypeDefId(): kotlin.UInt {
        return stub.lookupTableValue(CLRMetadataType.EXPORTED_TYPE, 1, kotlin.UInt::class)!!
    }

    override fun getTypeName(): kotlin.String {
        return stub.lookupString(CLRMetadataType.EXPORTED_TYPE, 2)
    }

    override fun getTypeNamespace(): kotlin.String {
        return stub.lookupString(CLRMetadataType.EXPORTED_TYPE, 3)
    }

    override fun getCustomAttribute(): com.github.danielchemko.winmdj.core.mdspec.CustomAttribute? {
        return getStub().computeReverseLookup(
            ExportedType::class,
            0,
            com.github.danielchemko.winmdj.core.mdspec.CustomAttribute::class,
            false,
        ) as com.github.danielchemko.winmdj.core.mdspec.CustomAttribute?
    }
}