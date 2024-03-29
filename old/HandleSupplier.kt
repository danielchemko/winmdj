package com.github.danielchemko.winmdj.util

import java.util.concurrent.Callable


interface HandleSupplier : AutoCloseable {
    /**
     * Returns a handle, possibly creating it lazily. A Handle holds a database connection, so extensions should only
     * call this method in order to interact with the database.
     *
     * @return An open Handle.
     */
    val handle: Handle


    /**
     * Bind a new [ExtensionContext] to the Handle, invoke the given task, then restore the Handle's extension state.
     *
     * @param <V>              the result type of the task
     * @param extensionContext An [ExtensionContext] object that manages the extension state.
     * @param task             the code to execute in an extension context
     * @return the callable's result
     * @throws Exception if any exception is thrown
    </V> */
    @Throws(Exception::class)
    fun <V> invokeInContext(extensionContext: ExtensionContext, task: Callable<V>?): V

    override fun close() {}
}