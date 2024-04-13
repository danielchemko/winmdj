package com.github.danielchemko.winmdj.java

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.*
import com.github.danielchemko.winmdj.parser.WinMdNavigator

class ModuleBuilder(
    private val navigator: WinMdNavigator
) {
    private val objectMapper = MdObjectMapper(navigator)
    val usedTypeRefs = mutableSetOf<TypeReference>()
    val usedTypeDefs = mutableSetOf<TypeDefinition>()
    val usedMethods = mutableSetOf<MethodDefinition>()

    fun doMagic() {
        objectMapper.getCursor(Module::class.java).forEach { module ->
            val types = module.getChildTypeReferences()
            usedTypeRefs.addAll(types)

            types.sortedWith(compareBy({ it.getNamespace() }, { it.getName() })).forEach { it ->
//                buildModuleType("", it)
            }
        }

        objectMapper.getCursor(AssemblyReference::class.java).forEach {
            val refChildTypes = it.getChildTypeReferences().subtract(usedTypeRefs)
            usedTypeRefs.addAll(refChildTypes)

            refChildTypes.sortedWith(compareBy({ it.getNamespace() }, { it.getName() })).forEach { assemblyRefTypes ->
                buildAssemblyRefType("", assemblyRefTypes)
            }
        }

        objectMapper.getCursor(ModuleReference::class.java).forEach {
            val refChildTypes = it.getChildTypeReferences().subtract(usedTypeRefs)
            usedTypeRefs.addAll(refChildTypes)

            refChildTypes.sortedWith(compareBy({ it.getNamespace() }, { it.getName() })).forEach { assemblyRefTypes ->
                buildModuleRefType("", assemblyRefTypes)
            }
        }

        println("Refs: ${usedTypeRefs.size} of ${navigator.getCount(CLRMetadataType.TYPE_REF)}")
        println("Defs: ${usedTypeDefs.size} of ${navigator.getCount(CLRMetadataType.TYPE_DEF)}")
        println("Methods: ${usedMethods.size} of ${navigator.getCount(CLRMetadataType.METHOD_DEF)}")
    }

    private fun buildModuleRefType(indent: String, typeRef: TypeReference) {
        println("${indent}MREFT: ${(typeRef.getResolutionScope() as AssemblyReference).getName()}/${typeRef.getNamespace()}/${typeRef.getName()}")
        renderType(indent, typeRef)
    }

    private fun buildAssemblyRefType(indent: String, typeRef: TypeReference) {
        println("${indent}AREFT: ${(typeRef.getResolutionScope() as AssemblyReference).getName()}/${typeRef.getNamespace()}/${typeRef.getName()}")
        renderType(indent, typeRef)
    }

    private fun buildModuleType(indent: String, typeRef: TypeReference) {
        println("${indent}MODT: ${(typeRef.getResolutionScope() as Module).getName()}/${typeRef.getNamespace()}/${typeRef.getName()}")
        renderType(indent, typeRef)
    }

    private fun buildSubType(indent: String, typeRef: TypeReference) {
        println("${indent}ST: ${typeRef.getNamespace()}/${typeRef.getName()}")
        renderType(indent, typeRef)
    }

    private fun renderType(indent: String, typeRef: TypeReference) {
        val refChildTypes = typeRef.getChildTypeReferences().subtract(usedTypeRefs)
        usedTypeRefs.addAll(refChildTypes)
        refChildTypes.forEach { child ->
            buildSubType("$indent   ", child)
        }

        val typeDefs = typeRef.getSubTypes();
        usedTypeDefs.addAll(typeDefs)
        typeDefs.forEach { typeDef ->
            buildTypeDef("$indent   ", typeDef)
        }
    }

    private fun buildTypeDef(indent: String, typeDef: TypeDefinition) {
        println("${indent}TypeDef: ${typeDef.getNamespace()}/${typeDef.getName()}")

        val methods = typeDef.getMethods()
        usedMethods.addAll(methods)
        methods.forEach { method ->
            println(
                "$indent   Method: ${
                    method.getName()
                }(${
                    method.getParameters().filter { it.getName() != "" }
                        .joinToString(",") { param ->
                            "${param.getFieldMarshal() ?: ""} ${param.getName()} ${
                                param.getConstant()?.let { "/* CON:${it.getValue()} */" } ?: ""
                            }"
                        }
                })}"
            )
        }
    }
}