package com.github.danielchemko.winmdj.util

import com.github.danielchemko.winmdj.core.mdspec.LookupType
import com.github.danielchemko.winmdj.core.mdspec.ObjectColumn
import com.github.danielchemko.winmdj.core.mdspec.ObjectType
import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import java.nio.file.Path
import kotlin.io.path.writer
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSuperclassOf

/**
 * If you're unable to run this class, you may need to delete your
 * src/main/kotlin/com/danielchemko/winmdj/core/autoobjects/stubs directory
 */
fun main(vararg args: String) {
    val writeToPath = Path.of("winmdj-core/src/main/kotlin/com/github/danielchemko/winmdj/core/autoobject/stubs")
    writeToPath.toFile().mkdirs()

    WinMdObject::class.sealedSubclasses.forEach { interfaceClazz ->
        try {
            val packageName = "com.github.danielchemko.winmdj.core.autoobject.stubs"
            val interfaceName = interfaceClazz.simpleName
            val concreteClassName = "Stub${interfaceName}Impl"
            val objectType = interfaceClazz.findAnnotation<ObjectType>()?.objectType
                ?: throw IllegalStateException("Type $interfaceClazz doesn't have an object type")

            val methodOverrides =
                interfaceClazz.supertypes.flatMap { interfaceClazz.functions.filter { it.hasAnnotation<ObjectColumn>() } }
                    .plus(
                        interfaceClazz.functions.filter { it.hasAnnotation<ObjectColumn>() }
                    ).toSet()

            val foundOrdinals = methodOverrides.mapNotNull { it.findAnnotation<ObjectColumn>()?.ordinal }.sorted()
            println("${objectType.name}: Ordinals:$foundOrdinals")

            val fileContents =
                """
            package $packageName
            
            
            import com.github.danielchemko.winmdj.core.MdObjectMapper
            import com.github.danielchemko.winmdj.core.autoobject.BaseWinMdStub
            import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
            import com.github.danielchemko.winmdj.core.mdspec.*
            import com.github.danielchemko.winmdj.parser.WinMdNavigator
            import javax.annotation.processing.Generated
            
            /**
             * THIS FILE IS AUTOMATICALLY GENERATED BY RegenerateSubs.kt. DO NOT EDIT IT BY HAND
             */
            
            @Generated
            class $concreteClassName (
                objectMapper: MdObjectMapper,
                navigator: WinMdNavigator,
                index: Int
            ) : $interfaceName {

                val stub = BaseWinMdStub(objectMapper, navigator, index)
                
                override fun toString(): String {
                   return "$interfaceName/${'$'}{getToken()}"
                }
                
                override fun getStub(): WinMdStub {
                    return stub                   
                }
                
                override fun getRowNumber(): Int {
                    return getStub().getRowNumber()
                }

                override fun getToken(): UInt {
                   return stub.getToken(CLRMetadataType.${objectType.name})
                }
                
                override fun getOffset(): UInt {
                   return stub.getObjectTableOffset(CLRMetadataType.${objectType.name}, 0).toUInt()
                }
                
                override fun copy(rowNum: Int): $concreteClassName {
                   return $concreteClassName::class.constructors.first().call(stub.getObjectMapper(), stub.getNavigator(), rowNum)
                }
       
            ${
                    methodOverrides.map { func ->
                        val column = func.findAnnotation<ObjectColumn>()!!
                        val table = column.table
                        val ordinal = column.ordinal
                        val subOrdinal = column.subOrdinal
                        val childListTerminator = column.childListTerminator
                        """
                            override fun ${func.name}(): ${func.returnType} {
                                ${
                            when (table) {
                                LookupType.TABLE_VALUE -> {
                                    val mandatorySuffix = if (!func.returnType.isMarkedNullable) "!!" else ""
                                    """
                                        return stub.lookupTableValue(CLRMetadataType.${objectType.name}, $ordinal, ${func.returnType}::class)$mandatorySuffix
                                    """.replaceIndent("")
                                }

                                LookupType.STRING -> {
                                    """
                                        return stub.lookupString(CLRMetadataType.${objectType.name}, $ordinal)
                                    """.replaceIndent("")
                                }

                                LookupType.BLOB -> {
                                    """
                                        return stub.lookupBlob(CLRMetadataType.${objectType.name}, $ordinal)
                                    """.replaceIndent("")
                                }

                                LookupType.GUID -> {
                                    """
                                        return stub.lookupGuid(CLRMetadataType.${objectType.name}, $ordinal)
                                    """.replaceIndent("")
                                }

                                LookupType.TARGET -> {
                                    val mandatorySuffix = if (!func.returnType.isMarkedNullable) "!!" else ""

                                    val returnClz = func.returnType.classifier as KClass<*>
                                    val returnClzQualified = returnClz.qualifiedName

                                    if (WinMdObject::class.isSuperclassOf(returnClz)) {
                                        """
                                            return stub.lookupConcreteReferent(CLRMetadataType.${objectType.name}, $ordinal, ${returnClzQualified}::class)$mandatorySuffix
                                        """.trimIndent()
                                    } else {
                                        """
                                            return stub.lookupInterfaceReferent(CLRMetadataType.${objectType.name}, $ordinal, ${returnClzQualified}::class)$mandatorySuffix
                                        """.trimIndent()
                                    }
                                }

                                LookupType.TARGET_LIST -> {
                                    // Pull out the type from the list generic
                                    val returnClz = (func.returnType.arguments[0]!!.type!!.classifier!! as KClass<*>)
                                    """
                                        val column = 
                                        return stub.lookupList(CLRMetadataType.${objectType.name}, $ordinal, ${column.subOrdinal}, ${column.childListTerminator}, ${returnClz.qualifiedName}::class)
                                    """.replaceIndent("")
                                }

                                LookupType.BITSET_ENUM -> {
                                    val returnClz = func.returnType.arguments[0]!!.type!!.classifier!! as KClass<*>
                                    val returnClzQualified = returnClz.qualifiedName
                                    """
                                        return stub.lookupBitsetEnum(CLRMetadataType.${objectType.name}, $ordinal, ${returnClzQualified}::class)
                                    """.trimIndent()
                                }

                                LookupType.BITSET -> {
                                    """
                                        return stub.lookupBitset(CLRMetadataType.${objectType.name}, $ordinal)
                                    """.trimIndent()
                                }

                                LookupType.REVERSE_TARGET -> {
                                    val returnType = func.returnType
                                    val isList = List::class.isSuperclassOf(returnType.classifier as KClass<*>)
                                    val returnClz = if (isList) {
                                        returnType.arguments[0].type!!.classifier!! as KClass<*>
                                    } else {
                                        returnType.classifier!! as KClass<*>
                                    }
                                    val mandatorySuffix = if (!func.returnType.isMarkedNullable) "!!" else ""
                                    """
                                        return getStub().computeReverseLookup(
                                            ${interfaceName}::class,
                                            $ordinal,
                                            ${returnClz.qualifiedName}::class,
                                            $isList,
                                        )$mandatorySuffix as ${func.returnType}
                                    """.trimIndent()
                                }
                            }
                        }
                            }
                        """.replaceIndent("")

                    }.joinToString("\n\n")
                }
            }
            """.trimIndent()

            writeToPath.resolve("${concreteClassName}.kt").writer().use { writer ->
                writer.write(fileContents)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
