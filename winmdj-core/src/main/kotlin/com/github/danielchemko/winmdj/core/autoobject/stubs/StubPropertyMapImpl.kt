package com.github.danielchemko.winmdj.core.autoobject.stubs


import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.autoobject.BaseWinMdStub
import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.PropertyMap
import com.github.danielchemko.winmdj.core.mdspec.WinMdStub
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import javax.annotation.processing.Generated

/**
 * THIS FILE IS AUTOMATICALLY GENERATED BY RegenerateSubs.kt. DO NOT EDIT IT BY HAND
 */

@Generated
class StubPropertyMapImpl(
    objectMapper: MdObjectMapper,
    navigator: WinMdNavigator,
    index: Int
) : PropertyMap {

    val stub = BaseWinMdStub(objectMapper, navigator, index)

    override fun toString(): String {
        return "PropertyMap/${getToken()}"
    }

    override fun getStub(): WinMdStub {
        return stub
    }

    override fun getRowNumber(): Int {
        return getStub().getRowNumber()
    }

    override fun getToken(): UInt {
        return stub.getToken(CLRMetadataType.PROPERTY_MAP)
    }

    override fun getOffset(): UInt {
        return stub.getObjectTableOffset(CLRMetadataType.PROPERTY_MAP, 0).toUInt()
    }

    override fun copy(rowNum: Int): StubPropertyMapImpl {
        return StubPropertyMapImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
    }

    override fun getParent(): com.github.danielchemko.winmdj.core.mdspec.TypeDefinition? {
        return stub.lookupConcreteReferent(
            CLRMetadataType.PROPERTY_MAP,
            0,
            com.github.danielchemko.winmdj.core.mdspec.TypeDefinition::class
        )
    }

    override fun getProperties(): kotlin.collections.List<com.github.danielchemko.winmdj.core.mdspec.Property> {
        val column =
            return stub.lookupList(
                CLRMetadataType.PROPERTY_MAP,
                1,
                -1,
                3,
                com.github.danielchemko.winmdj.core.mdspec.Property::class
            )
    }
}