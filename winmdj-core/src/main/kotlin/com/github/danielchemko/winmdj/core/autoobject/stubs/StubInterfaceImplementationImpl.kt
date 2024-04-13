            package com.github.danielchemko.winmdj.core.autoobject.stubs
            
            
            import com.github.danielchemko.winmdj.core.MdObjectMapper
            import com.github.danielchemko.winmdj.core.autoobject.WinMdStub
            import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
            import com.github.danielchemko.winmdj.core.mdspec.*
            import com.github.danielchemko.winmdj.parser.WinMdNavigator
            import javax.annotation.processing.Generated
            
            /**
             * THIS FILE IS AUTOMATICALLY GENERATED BY RegenerateSubs.kt. DO NOT EDIT IT BY HAND
             */
            
            @Generated
            class StubInterfaceImplementationImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : InterfaceImplementation {

                private val stub = WinMdStub(objectMapper, navigator, index)
                
                override fun toString(): String {
                   return "InterfaceImplementation/${getToken()}"
                }
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }
                
                override fun getRowNumber(): Int {
                    return getStub().getRowNumber()
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.INTERFACE_IMPL)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.INTERFACE_IMPL, 0).toUInt()
                }
                
                override fun copy(rowNum: Int?): StubInterfaceImplementationImpl {
                   return StubInterfaceImplementationImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum ?: getRowNumber())
                }
       
            override fun getInterface(): com.github.danielchemko.winmdj.core.mdspec.TypeDefOrRef {
    return stub.lookupInterfaceReferent(CLRMetadataType.INTERFACE_IMPL, 1, com.github.danielchemko.winmdj.core.mdspec.TypeDefOrRef::class)!!
}

override fun getTypeDefinition(): com.github.danielchemko.winmdj.core.mdspec.TypeDefinition {
    return stub.lookupConcreteReferent(CLRMetadataType.INTERFACE_IMPL, 0, com.github.danielchemko.winmdj.core.mdspec.TypeDefinition::class)!!
}

                            override fun getCustomAttribute(): com.github.danielchemko.winmdj.core.mdspec.CustomAttribute? {
                                return getStub().computeReverseLookup(
    InterfaceImplementation::class,
    0,
    com.github.danielchemko.winmdj.core.mdspec.CustomAttribute::class,
    false,
) as com.github.danielchemko.winmdj.core.mdspec.CustomAttribute?
                            }
            }