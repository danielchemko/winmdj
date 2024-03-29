package com.github.danielchemko.winmdj.util

import java.lang.reflect.Method

internal class DefaultMethodExtensionHandlerFactory : ExtensionHandlerFactory {
    companion object {
        val INSTANCE: ExtensionHandlerFactory = DefaultMethodExtensionHandlerFactory()
    }

    override fun accepts(extensionType: Class<*>, method: Method): Boolean {
        return extensionType.isInterface && method.isDefault() // interface default method
    }

    override fun createExtensionHandler(extensionType: Class<*>, method: Method): ExtensionHandler? {
        try {
            return ExtensionHandler.createForSpecialMethod(method)
        } catch (e: IllegalAccessException) {
            throw UnableToCreateExtensionException(
                "Default method handler for ${extensionType} couldn't unreflect ${method}",
                e
            )
        }
    }
}