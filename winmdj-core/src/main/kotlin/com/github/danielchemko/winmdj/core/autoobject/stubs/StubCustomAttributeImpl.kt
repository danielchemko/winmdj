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
            class StubCustomAttributeImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : CustomAttribute {

                private val stub = WinMdStub(objectMapper, navigator, index)
                
                override fun toString(): String {
                   return "CustomAttribute/${getToken()}"
                }
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }
                
                override fun getRowNumber(): Int {
                    return getStub().getRowNumber()
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.CUSTOM_ATTRIBUTE)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.CUSTOM_ATTRIBUTE, 0).toUInt()
                }
                
                override fun copy(rowNum: Int?): StubCustomAttributeImpl {
                   return StubCustomAttributeImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum ?: getRowNumber())
                }
       
            override fun getConstructor(): com.github.danielchemko.winmdj.core.mdspec.CustomAttributeType? {
    return stub.lookupInterfaceReferent(CLRMetadataType.CUSTOM_ATTRIBUTE, 1, com.github.danielchemko.winmdj.core.mdspec.CustomAttributeType::class)
}

override fun getParent(): com.github.danielchemko.winmdj.core.mdspec.HasCustomAttribute? {
    return stub.lookupInterfaceReferent(CLRMetadataType.CUSTOM_ATTRIBUTE, 0, com.github.danielchemko.winmdj.core.mdspec.HasCustomAttribute::class)
}

override fun getValue(): kotlin.ByteArray {
    return stub.lookupBlob(CLRMetadataType.CUSTOM_ATTRIBUTE, 2)
}
            }