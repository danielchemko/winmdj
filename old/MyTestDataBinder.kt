package com.github.danielchemko.winmdj.util

import com.github.danielchemko.winmdj.core.mdspec.Assembly

fun main(vararg args: String) {
    MyTestDataBinder()
}

class MyTestDataBinder {

    init {
        val factory = DefaultMethodExtensionHandlerFactory()

        val clazz = Assembly::class

        val handler = factory.createExtensionHandler(
            Assembly::class.java,
            MyTestDataBinder::class.java.declaredMethods.firstOrNull { it.name == "handleIt" }!!
        )

        println("Handler: $handler")
    }

    fun handleIt(vararg args: Any): Any {
        return TODO()
    }
}

