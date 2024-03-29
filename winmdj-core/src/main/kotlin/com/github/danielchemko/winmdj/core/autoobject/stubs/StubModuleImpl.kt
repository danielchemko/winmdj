            package com.github.danielchemko.winmdj.core.autoobject.stubs
            
            
            import com.github.danielchemko.winmdj.core.MdObjectMapper
            import com.github.danielchemko.winmdj.core.autoobject.BaseWinMdStub
            import com.github.danielchemko.winmdj.core.autoobject.model.CLRMetadataType
            import com.github.danielchemko.winmdj.core.mdspec.*
            import com.github.danielchemko.winmdj.parser.WinMdNavigator
            import javax.annotation.processing.Generated
            
            /**
             * THIS FILE IS AUTOMATICALLY GENERATED BY RegenerateSubs.kt. DO NOT EDIT IT BY HAND
             */
            
            @Generated
            class StubModuleImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : Module {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.MODULE)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.MODULE, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): StubModuleImpl {
                   return StubModuleImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            override fun getBaseGenerationId(): kotlin.ByteArray {
    return stub.lookupGuid(CLRMetadataType.MODULE, 2)
}

override fun getGeneration(): kotlin.UShort {
    return stub.lookupTableValue(CLRMetadataType.MODULE, 0, kotlin.UShort::class)!!
}

override fun getGenerationId(): kotlin.ByteArray {
    return stub.lookupGuid(CLRMetadataType.MODULE, 3)
}

override fun getMVid(): kotlin.ByteArray {
    return stub.lookupGuid(CLRMetadataType.MODULE, 4)
}

override fun getName(): kotlin.String {
    return stub.lookupString(CLRMetadataType.MODULE, 1)
}
            }