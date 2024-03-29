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
            class StubFieldImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : Field {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.FIELD)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.FIELD, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): StubFieldImpl {
                   return StubFieldImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            override fun getAttributes(): java.util.BitSet {
    return stub.lookupBitset(CLRMetadataType.FIELD, 0)
}

override fun getName(): kotlin.String {
    return stub.lookupString(CLRMetadataType.FIELD, 1)
}

override fun getSignature(): kotlin.ByteArray {
    return stub.lookupBlob(CLRMetadataType.FIELD, 2)
}

                            override fun getConstant(): com.github.danielchemko.winmdj.core.mdspec.Constant? {
                                val stubsCursor = getStub().getObjectMapper().getCursor(com.github.danielchemko.winmdj.core.mdspec.Field::class.java)
var rowRef = getStub().getRowNumber() - 1;
var highestMethod: com.github.danielchemko.winmdj.core.mdspec.Field = this
return getStub().getReverseReferentSingle(CLRMetadataType.CONSTANT, -1, com.github.danielchemko.winmdj.core.mdspec.Constant::class, highestMethod.getToken())
                            }

                            override fun getFieldMarshal(): com.github.danielchemko.winmdj.core.mdspec.FieldMarshal? {
                                val stubsCursor = getStub().getObjectMapper().getCursor(com.github.danielchemko.winmdj.core.mdspec.Field::class.java)
var rowRef = getStub().getRowNumber() - 1;
var highestMethod: com.github.danielchemko.winmdj.core.mdspec.Field = this
return getStub().getReverseReferentSingle(CLRMetadataType.FIELD_MARSHAL, -1, com.github.danielchemko.winmdj.core.mdspec.FieldMarshal::class, highestMethod.getToken())
                            }
            }