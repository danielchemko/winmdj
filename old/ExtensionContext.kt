package com.github.danielchemko.winmdj.util

import java.lang.reflect.Method

class ExtensionContext(val extensionMethod: ExtensionMethod) {

    companion object {
        fun forExtensionMethod(type: Class<*>, method: Method): ExtensionContext {
            return ExtensionContext(ExtensionMethod(type, method))
        }
    }
}