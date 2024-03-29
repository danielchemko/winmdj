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
            class StubTypeReferenceImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : TypeReference {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.TYPE_REF)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.TYPE_REF, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): StubTypeReferenceImpl {
                   return StubTypeReferenceImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            override fun getName(): kotlin.String {
    return stub.lookupString(CLRMetadataType.TYPE_REF, 1)
}

override fun getNamespace(): kotlin.String {
    return stub.lookupString(CLRMetadataType.TYPE_REF, 2)
}

override fun getResolutionScope(): com.github.danielchemko.winmdj.core.mdspec.ResolutionScope? {
    return stub.lookupInterfaceReferent(CLRMetadataType.TYPE_REF, 0, com.github.danielchemko.winmdj.core.mdspec.ResolutionScope::class)
}
            }