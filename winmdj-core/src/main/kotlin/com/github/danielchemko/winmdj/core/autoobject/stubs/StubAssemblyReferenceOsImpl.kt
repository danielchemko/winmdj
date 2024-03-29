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
            class StubAssemblyReferenceOsImpl (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : AssemblyReferenceOs {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.ASSEMBLY_REF_OS)
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.ASSEMBLY_REF_OS, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): StubAssemblyReferenceOsImpl {
                   return StubAssemblyReferenceOsImpl::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            override fun getAssemblyReference(): com.github.danielchemko.winmdj.core.mdspec.AssemblyReference? {
    return stub.lookupConcreteReferent(CLRMetadataType.ASSEMBLY_REF_OS, 3, com.github.danielchemko.winmdj.core.mdspec.AssemblyReference::class)
}

override fun getOSMajorVersion(): kotlin.UInt {
    return stub.lookupTableValue(CLRMetadataType.ASSEMBLY_REF_OS, 1, kotlin.UInt::class)!!
}

override fun getOSMinorVersion(): kotlin.UInt {
    return stub.lookupTableValue(CLRMetadataType.ASSEMBLY_REF_OS, 2, kotlin.UInt::class)!!
}

override fun getOSPlatformId(): kotlin.UInt {
    return stub.lookupTableValue(CLRMetadataType.ASSEMBLY_REF_OS, 0, kotlin.UInt::class)!!
}
            }