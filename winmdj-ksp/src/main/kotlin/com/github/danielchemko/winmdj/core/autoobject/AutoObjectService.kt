package com.github.danielchemko.winmdj.core.autoobject

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

class AutoObjectService : SymbolProcessor, SymbolProcessorProvider {

//    fun processSources() {
//        WinMdObject::class.sealedSubclasses.forEach { interfaceClazz ->
//            println("$interfaceClazz -> ${interfaceClazz}Impl")
//        }
//    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        println("Processing Stub Generation!!!")

        return emptyList()
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoObjectService()
    }
}

fun main(vararg args: String) {
//    AutoObjectService().processSources()
}