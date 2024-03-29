package com.github.danielchemko.winmdj.util

import java.lang.reflect.Method
import java.util.*


class ExtensionMethod(val type: Class<*>, val method: Method) {
    override fun toString(): String {
        return StringJoiner(", ", ExtensionMethod::class.java.simpleName + "[", "]")
            .add("type=$type")
            .add("method=$method")
            .toString()
    }
}