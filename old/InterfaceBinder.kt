package com.github.danielchemko.winmdj.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer

private val METHOD_HANDLE_CACHE: ConcurrentMap<Class<*>, MethodHandle> = ConcurrentHashMap()

fun <T> findConstructorAndCreateInstance(
    type: Class<T>,
    types: Array<Class<*>>,
    invoker: MethodHandleInvoker<T>
): T {
    try {
        val ctorHandle: MethodHandle = METHOD_HANDLE_CACHE.computeIfAbsent(type) { t ->
            findCtorMethodHandleForParameters(
                t,
                *types
            )
        }
        return invoker.createInstance(ctorHandle)
    } catch (t: Throwable) {
        throw RuntimeException(t)
    }
}

fun <T : Any> findConstructor(type: Class<T>, vararg types: Class<*>): MethodHandleHolder<T> {
    val ctorHandle = findCtorMethodHandleForParameters(type, *types)
    return MethodHandleHolder { invoker: MethodHandleInvoker<T> ->
        try {
            return@MethodHandleHolder invoker.createInstance(ctorHandle)
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }
    }
}

private fun findCtorMethodHandleForParameters(type: Class<*>, vararg types: Class<*>): MethodHandle {
    val suppressedThrowables = LinkedList<Throwable>()

    val constructors = type.constructors

    for (argCount in types.size downTo 0) {
        constructors@ for (constructor in constructors) {
            if (constructor.parameterCount != argCount) {
                continue
            }

            for (i in 0 until argCount) {
                if (!constructor.parameterTypes[i].isAssignableFrom(types[i])) {
                    continue@constructors
                }
            }

            try {
                var methodHandle = MethodHandles.lookup().unreflectConstructor(constructor)
                if (argCount < types.size) {
                    // the method handle will always be called with all possible arguments.
                    // Using dropArguments will remove any argument that the method handle not
                    // need (because the actual c'tor takes less arguments). This allows calling invokeExact because
                    // the exposed method handle will always take all arguments.
                    methodHandle = MethodHandles.dropArguments(
                        methodHandle, argCount,
                        *Arrays.copyOfRange(types, argCount, types.size)
                    )
                }
                return methodHandle.asType(methodHandle.type().changeReturnType(Any::class.java))
            } catch (e: IllegalAccessException) {
                suppressedThrowables.add(e)
            }
        }
    }

    val failure =
        NoSuchMethodException("No constructor for class '${type.name}', loosely matching arguments ${types.contentToString()}")
    suppressedThrowables.forEach(Consumer { exception: Throwable? ->
        failure.addSuppressed(
            exception
        )
    })

    // return a method handle that will throw the no such method exception on invocation, thus deferring
    // the actual exception until invocation time.
    return MethodHandles.dropArguments(
        MethodHandles.insertArguments(
            MethodHandles.throwException(
                Any::class.java,
                Exception::class.java
            ), 0, failure
        ),
        0, *types
    )
}

fun interface MethodHandleHolder<T> {
    fun invoke(invoker: MethodHandleInvoker<T>): T
}

fun interface MethodHandleInvoker<T> {
    @Throws(Throwable::class)
    fun createInstance(handle: MethodHandle): T
}