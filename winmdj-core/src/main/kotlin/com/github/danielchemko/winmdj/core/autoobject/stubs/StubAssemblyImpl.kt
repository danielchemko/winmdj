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
            class StubAssemblyImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : Assembly {

                private val stub = WinMdStub(objectMapper, navigator, index)
                
                override fun toString(): String {
                   return "Assembly/${getToken()}"
                }
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }
                
                override fun getRowNumber(): Int {
                    return getStub().getRowNumber()
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.ASSEMBLY)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.ASSEMBLY, 0).toUInt()
                }
                
                override fun copy(rowNum: Int?): StubAssemblyImpl {
                   return StubAssemblyImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum ?: getRowNumber())
                }
       
            override fun getCulture(): kotlin.String {
    return stub.lookupString(CLRMetadataType.ASSEMBLY, 5)
}

override fun getFlags(): kotlin.collections.List<com.github.danielchemko.winmdj.core.mdspec.AssemblyFlags> {
    return stub.lookupBitsetEnum(CLRMetadataType.ASSEMBLY, 2, com.github.danielchemko.winmdj.core.mdspec.AssemblyFlags::class)
}

override fun getHashAlgorithmId(): kotlin.collections.List<com.github.danielchemko.winmdj.core.mdspec.AssemblyHashAlgorithm> {
    return stub.lookupBitsetEnum(CLRMetadataType.ASSEMBLY, 0, com.github.danielchemko.winmdj.core.mdspec.AssemblyHashAlgorithm::class)
}

override fun getName(): kotlin.String {
    return stub.lookupString(CLRMetadataType.ASSEMBLY, 4)
}

override fun getPublicKey(): kotlin.ByteArray {
    return stub.lookupBlob(CLRMetadataType.ASSEMBLY, 3)
}

override fun getVersionRaw(): kotlin.ULong {
    return stub.lookupTableValue(CLRMetadataType.ASSEMBLY, 1, kotlin.ULong::class)!!
}

                            override fun getCustomAttribute(): com.github.danielchemko.winmdj.core.mdspec.CustomAttribute? {
                                return getStub().computeReverseLookup(
    Assembly::class,
    0,
    com.github.danielchemko.winmdj.core.mdspec.CustomAttribute::class,
    false,
) as com.github.danielchemko.winmdj.core.mdspec.CustomAttribute?
                            }

                            override fun getSecurityAttribute(): com.github.danielchemko.winmdj.core.mdspec.SecurityAttribute? {
                                return getStub().computeReverseLookup(
    Assembly::class,
    1,
    com.github.danielchemko.winmdj.core.mdspec.SecurityAttribute::class,
    false,
) as com.github.danielchemko.winmdj.core.mdspec.SecurityAttribute?
                            }
            }