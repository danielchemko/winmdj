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
            class StubStandAloneMethodSignatureImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : StandAloneMethodSignature {

                private val stub = WinMdStub(objectMapper, navigator, index)
                
                override fun toString(): String {
                   return "StandAloneMethodSignature/${getToken()}"
                }
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }
                
                override fun getRowNumber(): Int {
                    return getStub().getRowNumber()
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.STAND_ALONE_SIG)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.STAND_ALONE_SIG, 0).toUInt()
                }
                
                override fun copy(rowNum: Int?): StubStandAloneMethodSignatureImpl {
                   return StubStandAloneMethodSignatureImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum ?: getRowNumber())
                }
       
            override fun getSignature(): kotlin.ByteArray {
    return stub.lookupBlob(CLRMetadataType.STAND_ALONE_SIG, 0)
}

                            override fun getCustomAttribute(): com.github.danielchemko.winmdj.core.mdspec.CustomAttribute? {
                                return getStub().computeReverseLookup(
    StandAloneMethodSignature::class,
    0,
    com.github.danielchemko.winmdj.core.mdspec.CustomAttribute::class,
    false,
) as com.github.danielchemko.winmdj.core.mdspec.CustomAttribute?
                            }
            }