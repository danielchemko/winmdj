package com.github.danielchemko.winmdj.core.mdspec

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf

fun getColumnIndex(type: CLRMetadataType, propertyName: String): Int {
    val clazz = getClassInterface(type)
    val memberFunction = clazz.functions.firstOrNull { it.name == propertyName }
        ?: throw IllegalStateException("Unable to find a property in $clazz for $propertyName")
    return memberFunction.findAnnotation<ObjectColumn>()?.ordinal
        ?: throw IllegalStateException("Unable to find an ObjectColumn in $clazz for $propertyName")
}

fun getClassInterface(type: CLRMetadataType): KClass<out WinMdObject> {
    return WinMdObject::class.sealedSubclasses.filter { it.findAnnotation<ObjectType>()?.objectType == type }
        .firstOrNull() ?: throw IllegalStateException("Unable to find an instance of $type")
}

/* Hold object types from classes */
private val interfaceSpecCache: MutableMap<KClass<out WinMdCompositeReference>, InterfaceSpec> = ConcurrentHashMap()

fun getInterfaceSpec(clazz: KClass<out WinMdCompositeReference>): InterfaceSpec {
    return interfaceSpecCache.computeIfAbsent(clazz) { v ->
        v.findAnnotation<InterfaceSpec>()
            ?: v.supertypes.firstNotNullOf { (it.classifier as KClass<*>).findAnnotation<InterfaceSpec>() }
    }
}

/* Hold object types from classes */
private val objectTypeCache: MutableMap<KClass<out WinMdObject>, ObjectType> = ConcurrentHashMap()

fun getObjectType(clazz: KClass<out WinMdObject>): ObjectType {
    return objectTypeCache.computeIfAbsent(clazz) { v ->
        v.findAnnotation<ObjectType>()
            ?: v.supertypes.firstNotNullOf { (it.classifier as KClass<*>).findAnnotation<ObjectType>() }
    }
}

/* Hold object column info from classes/columns */
private val objectColumnCache: MutableMap<Pair<KClass<*>, Int>, ObjectColumn> = ConcurrentHashMap()

fun getColumnInfo(clazz: KClass<*>, column: Int): ObjectColumn {
    return objectColumnCache.computeIfAbsent(clazz to column) { (clazz, column) ->
        return@computeIfAbsent colInfoFromClass(clazz, column)
            ?: clazz.supertypes.map { it.classifier as KClass<*> }
                .firstNotNullOf { superClz -> colInfoFromClass(superClz, column) }
    }
}

private fun colInfoFromClass(clazz: KClass<*>, column: Int): ObjectColumn? {
    return clazz.functions.firstNotNullOfOrNull { func ->
        val col = func.findAnnotation<ObjectColumn>() ?: return@firstNotNullOfOrNull null
        if (col.ordinal != column) {
            null
        } else {
            col
        }
    }
}

/* Hold the base type of this column */
private val columnBaseCache: MutableMap<Pair<KClass<*>, Int>, KClass<*>> = ConcurrentHashMap()

fun getColumnBaseClassType(clazz: KClass<*>, column: Int): KClass<*> {
    return columnBaseCache.computeIfAbsent(clazz to column) { (clazz, column) ->
        return@computeIfAbsent columnBaseClassType(clazz, column)
            ?: clazz.supertypes.map { it.classifier as KClass<*> }
                .firstNotNullOf { superClz -> columnBaseClassType(superClz, column) }
    }
}

private fun columnBaseClassType(clazz: KClass<*>, column: Int): KClass<*>? {
    return clazz.functions.firstNotNullOfOrNull { func ->
        val col = func.findAnnotation<ObjectColumn>() ?: return@firstNotNullOfOrNull null
        if (col.ordinal != column) {
            null
        } else {
            func.returnType
        }
    }?.let { returnType ->
        val returnClassFinal = if (List::class.isSuperclassOf(returnType.classifier as KClass<*>)) {
            returnType.arguments[0].type!!.classifier as KClass<*>
        } else {
            returnType.classifier as KClass<*>
        }
        if (WinMdObject::class.isSuperclassOf(returnClassFinal)) {
            WinMdObject::class
        } else if (WinMdCompositeReference::class.isSuperclassOf(returnClassFinal)) {
            WinMdCompositeReference::class
        } else {
            throw IllegalStateException("Class $clazz is neither a WinMdObject or WinMdCompositeReference")
        }
    }
}

/* Hold the base type of this column */
private val columnClassCache: MutableMap<Pair<KClass<*>, Int>, KClass<*>?> = ConcurrentHashMap()

fun getColumnClassType(clazz: KClass<*>, column: Int): KClass<*>? {
    return columnClassCache.computeIfAbsent(clazz to column) { (clazz, column) ->
        return@computeIfAbsent columnClassType(clazz, column)
            ?: clazz.supertypes.map { it.classifier as KClass<*> }
                .firstNotNullOfOrNull { superClz -> columnClassType(superClz, column) }
    }
}

private fun columnClassType(clazz: KClass<*>, column: Int): KClass<*>? {
    return clazz.functions.firstNotNullOfOrNull { func ->
        val col = func.findAnnotation<ObjectColumn>() ?: return@firstNotNullOfOrNull null
        if (col.ordinal != column) {
            null
        } else {
            func.returnType
        }
    }?.let { returnType ->
        return if (List::class.isSuperclassOf(returnType.classifier as KClass<*>)) {
            returnType.arguments[0].type!!.classifier as KClass<*>
        } else {
            returnType.classifier as KClass<*>
        }
    }
}