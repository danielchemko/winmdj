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
            class StubFieldLayoutImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : FieldLayout {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.FIELD_LAYOUT)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.FIELD_LAYOUT, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): StubFieldLayoutImpl {
                   return StubFieldLayoutImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            override fun getField(): com.github.danielchemko.winmdj.core.mdspec.Field? {
    return stub.lookupConcreteReferent(CLRMetadataType.FIELD_LAYOUT, 1, com.github.danielchemko.winmdj.core.mdspec.Field::class)
}

override fun getFieldOffset(): kotlin.UInt {
    return stub.lookupTableValue(CLRMetadataType.FIELD_LAYOUT, 0, kotlin.UInt::class)!!
}
            }