package com.github.danielchemko.winmdj.core.autoobject.stubs


import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.autoobject.BaseWinMdStub
import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.MethodDefinition
import com.github.danielchemko.winmdj.core.mdspec.WinMdStub
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import javax.annotation.processing.Generated

/**
 * THIS FILE IS AUTOMATICALLY GENERATED BY RegenerateSubs.kt. DO NOT EDIT IT BY HAND
 */

@Generated
class StubMethodDefinitionImpl(
    objectMapper: MdObjectMapper,
    navigator: WinMdNavigator,
    index: Int
) : MethodDefinition {

    val stub = BaseWinMdStub(objectMapper, navigator, index)

    override fun toString(): String {
        return "MethodDefinition/${getToken()}"
    }

    override fun getStub(): WinMdStub {
        return stub
    }

    override fun getRowNumber(): Int {
        return getStub().getRowNumber()
    }

    override fun getToken(): UInt {
        return stub.getToken(CLRMetadataType.METHOD_DEF)
    }

    override fun getOffset(): UInt {
        return stub.getObjectTableOffset(CLRMetadataType.METHOD_DEF, 0).toUInt()
    }

    override fun copy(rowNum: Int): StubMethodDefinitionImpl {
        return StubMethodDefinitionImpl::class.constructors.first()
            .call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
    }

    override fun getAttributes(): java.util.BitSet {
        return stub.lookupBitset(CLRMetadataType.METHOD_DEF, 0)
    }

    override fun getImplementationAttributes(): java.util.BitSet {
        return stub.lookupBitset(CLRMetadataType.METHOD_DEF, 1)
    }

    override fun getName(): kotlin.String {
        return stub.lookupString(CLRMetadataType.METHOD_DEF, 3)
    }

    override fun getParameters(): kotlin.collections.List<com.github.danielchemko.winmdj.core.mdspec.Parameter> {
        val column =
            return stub.lookupList(
                CLRMetadataType.METHOD_DEF,
                5,
                1,
                1,
                com.github.danielchemko.winmdj.core.mdspec.Parameter::class
            )
    }

    override fun getParent(): com.github.danielchemko.winmdj.core.mdspec.TypeDefinition? {
        return getStub().computeReverseLookup(
            MethodDefinition::class,
            5,
            com.github.danielchemko.winmdj.core.mdspec.TypeDefinition::class,
            false,
        ) as com.github.danielchemko.winmdj.core.mdspec.TypeDefinition?
    }

    override fun getRva(): kotlin.UShort {
        return stub.lookupTableValue(CLRMetadataType.METHOD_DEF, 2, kotlin.UShort::class)!!
    }

    override fun getSignature(): kotlin.ByteArray {
        return stub.lookupBlob(CLRMetadataType.METHOD_DEF, 4)
    }

    override fun getCustomAttribute(): com.github.danielchemko.winmdj.core.mdspec.CustomAttribute? {
        return getStub().computeReverseLookup(
            MethodDefinition::class,
            0,
            com.github.danielchemko.winmdj.core.mdspec.CustomAttribute::class,
            false,
        ) as com.github.danielchemko.winmdj.core.mdspec.CustomAttribute?
    }

    override fun getGenericParameters(): kotlin.collections.List<com.github.danielchemko.winmdj.core.mdspec.GenericParameter> {
        return getStub().computeReverseLookup(
            MethodDefinition::class,
            2,
            com.github.danielchemko.winmdj.core.mdspec.GenericParameter::class,
            true,
        )!! as kotlin.collections.List<com.github.danielchemko.winmdj.core.mdspec.GenericParameter>
    }

    override fun getImplementationMap(): com.github.danielchemko.winmdj.core.mdspec.ImplementationMap? {
        return getStub().computeReverseLookup(
            MethodDefinition::class,
            1,
            com.github.danielchemko.winmdj.core.mdspec.ImplementationMap::class,
            false,
        ) as com.github.danielchemko.winmdj.core.mdspec.ImplementationMap?
    }

    override fun getMemberReference(): com.github.danielchemko.winmdj.core.mdspec.MemberReference? {
        return getStub().computeReverseLookup(
            MethodDefinition::class,
            0,
            com.github.danielchemko.winmdj.core.mdspec.MemberReference::class,
            false,
        ) as com.github.danielchemko.winmdj.core.mdspec.MemberReference?
    }

    override fun getSecurityAttribute(): com.github.danielchemko.winmdj.core.mdspec.SecurityAttribute? {
        return getStub().computeReverseLookup(
            MethodDefinition::class,
            1,
            com.github.danielchemko.winmdj.core.mdspec.SecurityAttribute::class,
            false,
        ) as com.github.danielchemko.winmdj.core.mdspec.SecurityAttribute?
    }
}