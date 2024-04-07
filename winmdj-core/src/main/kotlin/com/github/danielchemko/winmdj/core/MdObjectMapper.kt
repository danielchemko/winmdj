package com.github.danielchemko.winmdj.core

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.ObjectType
import com.github.danielchemko.winmdj.core.mdspec.WinMdCompositeReference
import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class MdObjectMapper(
    private val navigator: WinMdNavigator
) {
    /**
     * NOTE: To save RAM, the objects returned from this cursor are reused facades in fronts of the RAW data. This means
     * that the WinMdObjects should never be stored directly. In order to store a copy of this data, you must first copy
     * the object by calling myWinMdObject.clone() first.
     */
    fun <T : WinMdObject> getCursor(clazz: Class<T>): WinMdObjectCursor<T> {
        return WinMdObjectCursor(this, navigator, clazz)
    }

    fun <T : WinMdCompositeReference> getInterfaceCursor(clazz: Class<T>): WinMdInterfaceCursor<T> {
        return WinMdInterfaceCursor(this, navigator, clazz)
    }
}

data class ClassMeta<T : Any>(val kClass: KClass<T>, val implClass: KClass<out Any>, val type: CLRMetadataType)

val classMetadataCache: LoadingCache<Class<*>, ClassMeta<*>> = Caffeine.newBuilder().build { clazz ->
    val kClass = clazz.kotlin
    ClassMeta(
        kClass,
        Class.forName("com.github.danielchemko.winmdj.core.autoobject.stubs.Stub${kClass.simpleName}Impl").kotlin,
        kClass.findAnnotation<ObjectType>()!!.objectType
    )
}

class WinMdObjectCursor<T : WinMdObject>(
    private val objectMapper: MdObjectMapper,
    private val navigator: WinMdNavigator,
    private val clazz: Class<T>
) {
    private val classMeta = classMetadataCache.get(clazz)
    private val count = navigator.getCount(classMeta.type)

    fun get(rowId: Int): T {
        assert(rowId > 0)
        assert(rowId <= count)

        return createStub(rowId)
    }

    fun getRowCount(): Int {
        return count
    }

    fun find(predicate: (T) -> Boolean): T? {
        return map { it }.filter(predicate).firstOrNull()
    }

    fun <R> map(mapper: (T) -> R): Sequence<R> {
        val stub = createStub(1)
        return (1..count).asSequence().map { idx ->
            stub.getStub().setRowNumberIndex(idx)
            mapper.invoke(stub)
        }
    }

    fun forEach(consumer: (T) -> Unit) {
        map { it }.forEach(consumer)
    }

    private fun createStub(index: Int): T {
        assert(index > 0)
        return classMeta.implClass.primaryConstructor!!.call(objectMapper, navigator, index) as T
    }
}

class WinMdInterfaceCursor<T : WinMdCompositeReference>(
    private val objectMapper: MdObjectMapper,
    private val navigator: WinMdNavigator,
    private val interfaceClazz: Class<T>
) {
    private val kClasses: List<KClass<out T>> = interfaceClazz.classes.map { it.kotlin as KClass<out T> }
    private val implClasses: List<KClass<out T>> =
        kClasses.map { kClass -> Class.forName("com.github.danielchemko.winmdj.core.autoobject.stubs.Stub${kClass.simpleName}Impl").kotlin as KClass<T> }
    private val types: List<CLRMetadataType> = kClasses.map { it.findAnnotation<ObjectType>()!!.objectType }
    private val count = types.sumOf { navigator.getCount(it) }

    fun getRowCount(): Int {
        return count
    }

    @Suppress("UNCHECKED_CAST") // This all makes sense, TRUST ME!
    fun <R> map(mapper: (WinMdObject) -> R): Sequence<R> {
        return implClasses.flatMap { clazz ->
            val stub = createStub(clazz, 1)
            (1..count).asSequence()
                .map { idx ->
                    (stub as WinMdObject).getStub().setRowNumberIndex(idx)
                    mapper.invoke(stub as WinMdObject)
                }
        }.asSequence()
    }

    fun find(predicate: (WinMdObject) -> Boolean): WinMdObject? {
        return map { it }.filter(predicate).firstOrNull()
    }

    fun forEach(consumer: (WinMdObject) -> Unit) {
        map { it }.forEach(consumer)
    }

    private fun createStub(kClass: KClass<*>, index: Int): Any {
        assert(index > 0)
        return kClass.primaryConstructor!!.call(objectMapper, navigator, index)
    }
}
