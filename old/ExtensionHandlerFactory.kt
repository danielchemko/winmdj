package com.github.danielchemko.winmdj.util


import java.lang.reflect.Method
import java.util.*

/**
 * A factory to create [ExtensionHandler] instances.
 */
interface ExtensionHandlerFactory {
    /**
     * Determines whether the factory can create an [ExtensionHandler] for combination of extension type and method.
     *
     * @param extensionType The extension type class
     * @param method        A method
     * @return True if the factory can create an extension handler for extension type and method, false otherwise
     */
    fun accepts(extensionType: Class<*>, method: Method): Boolean

    /**
     * Returns an [ExtensionHandler] instance for a extension type and method combination.
     *
     * @param extensionType The extension type class
     * @param method        A method
     * @return An [ExtensionHandler] instance wrapped into an [Optional]. The optional can be empty. This is necessary to retrofit old code
     * that does not have an accept/build code pair but unconditionally tries to build a handler and returns empty if it can not. New code should always
     * return `Optional.of(extensionHandler}` and never return `Optional.empty()`
     */
    fun createExtensionHandler(extensionType: Class<*>, method: Method): ExtensionHandler?
}