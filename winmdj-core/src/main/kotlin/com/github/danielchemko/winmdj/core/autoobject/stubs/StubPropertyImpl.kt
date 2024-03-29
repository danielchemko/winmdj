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
            class StubPropertyImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : Property {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.PROPERTY)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.PROPERTY, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): StubPropertyImpl {
                   return StubPropertyImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            override fun getAttributes(): java.util.BitSet {
    return stub.lookupBitset(CLRMetadataType.PROPERTY, 0)
}

                            override fun getMethodSemantic(): com.github.danielchemko.winmdj.core.mdspec.MethodSemantics? {
                                val stubsCursor = getStub().getObjectMapper().getCursor(com.github.danielchemko.winmdj.core.mdspec.Property::class.java)
var rowRef = getStub().getRowNumber() - 1;
var highestMethod: com.github.danielchemko.winmdj.core.mdspec.Property = this
return getStub().getReverseReferentSingle(CLRMetadataType.METHOD_SEMANTICS, -1, com.github.danielchemko.winmdj.core.mdspec.MethodSemantics::class, highestMethod.getToken())
                            }

override fun getName(): kotlin.String {
    return stub.lookupString(CLRMetadataType.PROPERTY, 1)
}

                            override fun getParent(): com.github.danielchemko.winmdj.core.mdspec.EventMap {
                                val stubsCursor = getStub().getObjectMapper().getCursor(com.github.danielchemko.winmdj.core.mdspec.Property::class.java)
var rowRef = getStub().getRowNumber() - 1;
var highestMethod: com.github.danielchemko.winmdj.core.mdspec.Property = this
return getStub().getReverseReferentSingle(CLRMetadataType.EVENT_MAP, -1, com.github.danielchemko.winmdj.core.mdspec.EventMap::class, highestMethod.getToken())!!
                            }

override fun getSignature(): kotlin.ByteArray {
    return stub.lookupBlob(CLRMetadataType.PROPERTY, 2)
}

                            override fun getConstant(): com.github.danielchemko.winmdj.core.mdspec.Constant? {
                                val stubsCursor = getStub().getObjectMapper().getCursor(com.github.danielchemko.winmdj.core.mdspec.Property::class.java)
var rowRef = getStub().getRowNumber() - 1;
var highestMethod: com.github.danielchemko.winmdj.core.mdspec.Property = this
return getStub().getReverseReferentSingle(CLRMetadataType.CONSTANT, -1, com.github.danielchemko.winmdj.core.mdspec.Constant::class, highestMethod.getToken())
                            }

                            override fun getSemantics(): com.github.danielchemko.winmdj.core.mdspec.MethodSemantics {
                                val stubsCursor = getStub().getObjectMapper().getCursor(com.github.danielchemko.winmdj.core.mdspec.Property::class.java)
var rowRef = getStub().getRowNumber() - 1;
var highestMethod: com.github.danielchemko.winmdj.core.mdspec.Property = this
return getStub().getReverseReferentSingle(CLRMetadataType.METHOD_SEMANTICS, -1, com.github.danielchemko.winmdj.core.mdspec.MethodSemantics::class, highestMethod.getToken())!!
                            }
            }