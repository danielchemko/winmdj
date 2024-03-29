package com.github.danielchemko.winmdj.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

fun interface ExtensionHandler {
    /**
     * Gets invoked to return a value for the method that this handler was bound to.
     * @param handleSupplier A [HandleSupplier] instance for accessing the handle and its related objects
     * @param target The target object on which the handler should operate
     * @param args Optional arguments for the handler
     * @return The return value for the method that was bound to the extension handler. Can be null
     * @throws Exception Any exception from the underlying code
     */
    @Throws(Exception::class)
    fun invoke(handleSupplier: HandleSupplier, target: Any, vararg args: Any): Any?

    companion object {
        /**
         * Returns a default handler for missing functionality. The handler will throw an exception when invoked.
         * @param method The method to which this specific handler instance is bound
         * @return An [ExtensionHandler] instance
         */
        fun missingExtensionHandler(method: Method): ExtensionHandler {
            return ExtensionHandler { target: HandleSupplier, args: Any, handleSupplier: Array<out Any> ->
                throw IllegalStateException(
                    "Method ${
                        method.getDeclaringClass().getSimpleName()
                    }.${method.getName()} has no registered extension handler!"
                )
            }
        }

        /**
         * Create an extension handler and bind it to a method that will be called on the
         * target object when invoked.
         * @param method The [Method] to bind to
         * @return An [ExtensionHandler]
         * @throws IllegalAccessException If the method could not be unreflected
         */
        @Throws(IllegalAccessException::class)
        fun createForMethod(method: Method): ExtensionHandler {
            val declaringClass: Class<*> = method.getDeclaringClass()
            val methodHandle = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup()).unreflect(method)
            return createForMethodHandle(methodHandle)
        }

        /**
         * Create an extension handler and bind it to a special method that will be called on the
         * target object when invoked. This is needed e.g. for interface default methods.
         * @param method The [Method] to bind to
         * @return An [ExtensionHandler]
         * @throws IllegalAccessException If the method could not be unreflected
         */
        @Throws(IllegalAccessException::class)
        fun createForSpecialMethod(method: Method): ExtensionHandler {
            val declaringClass: Class<*> = method.getDeclaringClass()
            val methodHandle = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup())
                .unreflectSpecial(method, declaringClass)
            return createForMethodHandle(methodHandle)
        }

        /**
         * Create an extension handler and bind it to a [MethodHandle] instance.
         * @param methodHandle The [MethodHandle] to bind to
         * @return An [ExtensionHandler]
         */
        fun createForMethodHandle(methodHandle: MethodHandle): ExtensionHandler {
            return ExtensionHandler { handleSupplier: HandleSupplier, target: Any, args: Array<out Any> ->
                Unchecked.function { innerArgs: Array<out Any> ->
                    methodHandle.bindTo(target).invokeWithArguments(innerArgs) as Any
                }.apply(args)
            }
        }

        /** Implementation for the [Object.equals] method. Each object using this handler is only equal to itself.  */
        val EQUALS_HANDLER: ExtensionHandler =
            ExtensionHandler { handleSupplier: HandleSupplier, target: Any, args: Array<out Any> ->
                target === args[0]
            }

//        handleSupplier: HandleSupplier, target: Any, vararg args: Any): Any

        /** Implementation for the [Object.hashCode] method.  */
        val HASHCODE_HANDLER: ExtensionHandler =
            ExtensionHandler { handleSupplier: HandleSupplier, target: Any, args: Array<out Any> ->
                System.identityHashCode(target) as Any
            }


        /** Handler that only returns null independent of any input parameters.  */
        val NULL_HANDLER: ExtensionHandler =
            ExtensionHandler { handleSupplier: HandleSupplier, target: Any, args: Array<out Any> -> null }
    }
}