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
                
                override fun getStub(): WinMdStub {
                    return stub                   
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
                                        return getStub().computeReverseLookup(CLRMetadataType.${objectType.name},
                                            ${interfaceName}::class,
                                            $ordinal,
                                            $subOrdinal,
                                            $childListTerminator,
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


//                                    // Pull out the type from the list generic
//                                    var returnClz = func.returnType.classifier!! as KClass<*>
//                                    if (List::class.isSuperclassOf(returnClz)) {
//                                        returnClz = func.returnType.arguments[0]!!.type!!.classifier!! as KClass<*>
//                                        val returnClzQualified = returnClz.qualifiedName
//
//                                        if (WinMdObject::class.isSuperclassOf(returnClz)) {
//                                            val type = returnClz.findAnnotation<ObjectType>()!!.objectType
//
//                                            // TODO Find each item in the compatible type return list
//                                            """
//                                                val selfToken = getStub().getRowNumber()
//                                                val foreignCursor = getStub().getObjectMapper().getCursor(${returnClzQualified}::class.java)
//
//                                                val max = getStub().getNavigator().getCount(CLRMetadataType.${type.name})
//                                                return (1 .. max)
//                                                   .filter {row-> getStub().getRandomObjectTableValue(CLRMetadataType.${type.name}, row, 0) == selfToken }
//                                                   .map{row->foreignCursor.get(row)}.toList()
//                                            """.trimIndent()
//                                        } else {
//                                            // TODO Find each list of results from each compatible type and merge them
//                                            """
//                                                TODO()
//                                                val foundItems = getObjectMapper().map { it }.filter { getValueFor() }
//                                            """.trimIndent()
//                                        }
//                                    } else {
//                                        val returnObjectType = returnClz.findAnnotation<ObjectType>()!!.objectType
//                                        val mandatorySuffix = if (!func.returnType.isMarkedNullable) "!!" else ""
//                                        if (WinMdObject::class.isSuperclassOf(returnClz)) {
//                                            if (column.childListTerminator == CHILD_LIST_TERMINATOR_REPEATING) {
//                                                """
//                                                val stubsCursor = getStub().getObjectMapper().getCursor(${interfaceClazz.qualifiedName}::class.java)
//                                                var rowRef = getStub().getRowNumber() - 1;
//                                                var highestMethod: ${interfaceClazz.qualifiedName} = this
//                                                while (rowRef >= 0) {
//                                                    val refMethod = stubsCursor.get(rowRef--)
//                                                    if (refMethod.getStub().getObjectTableValue(CLRMetadataType.${objectType.name}, $subOrdinal) != getStub().getObjectTableValue(CLRMetadataType.${objectType.name}, $subOrdinal)) {
//                                                        return getStub().getReverseReferentSingle(CLRMetadataType.${returnObjectType.name},  ${tableColumn}, ${returnClz.qualifiedName}::class, highestMethod.getToken())$mandatorySuffix
//                                                    } else {
//                                                        highestMethod = refMethod
//                                                    }
//                                                }
//                                                throw IllegalStateException("Unable to find a parent reference for Method:[${'$'}highestMethod]")
//                                                """.trimIndent()
//                                            } else if (column.childListTerminator == CHILD_LIST_TERMINATOR_ASCENDING) {
//                                                """
//                                                val stubsCursor = getStub().getObjectMapper().getCursor(${interfaceClazz.qualifiedName}::class.java)
//                                                var rowRef = getStub().getRowNumber() - 1;
//                                                var highestMethod: ${interfaceClazz.qualifiedName} = this
//                                                while (rowRef >= 0) {
//                                                    val refMethod = stubsCursor.get(rowRef--)
//                                                    if (refMethod.getStub().getObjectTableValue(CLRMetadataType.${objectType.name}, $subOrdinal) > highestMethod.getStub().getObjectTableValue(CLRMetadataType.${objectType.name},$subOrdinal)) {
//                                                        return getStub().getReverseReferentSingle(CLRMetadataType.${returnObjectType.name}, ${tableColumn}, ${returnClz.qualifiedName}::class, highestMethod.getToken())$mandatorySuffix
//                                                    } else {
//                                                        highestMethod = refMethod
//                                                    }
//                                                }
//                                                throw IllegalStateException("Unable to find a parent reference for Method:[${'$'}highestMethod]")
//                                                """.trimIndent()
//                                            } else {
//                                                """
//                                                val stubsCursor = getStub().getObjectMapper().getCursor(${interfaceClazz.qualifiedName}::class.java)
//                                                var highestMethod: ${interfaceClazz.qualifiedName} = this
//                                                return getStub().getReverseReferentSingle(CLRMetadataType.${returnObjectType.name}, ${tableColumn}, ${returnClz.qualifiedName}::class, highestMethod.getToken())$mandatorySuffix
//                                                """.trimIndent()
//                                            }
//                                        } else {
//                                            // TODO Find first instance of the matching value within the list of candidate objects
//                                            """
//                                                return ${returnClz.qualifiedName}::class.sealedSubclasses.firstNotNullOf { clazz ->
//                                                    stub.getObjectMapper().getInterfaceCursor(${returnClz.qualifiedName}::class.java).map { it }.firstOrNull()
//                                                }
//                                            """.trimIndent()
//                                        }
//                                    }