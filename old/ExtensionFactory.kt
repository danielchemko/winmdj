package com.github.danielchemko.winmdj.util


fun interface ExtensionFactory {
    /**
     * Flags that the factory can return to control aspects of the extension framework.
     *
     * @since 3.38.0
     */
    enum class FactoryFlag {
        /**
         * The factory provides a concrete instance to back the extension type.
         * <br></br>
         * Unless this flag is present, factories do not
         * create an object to attach to but register method handlers for every method on an extension type.
         * <br></br>
         * E.g. the SQLObject handler is an example of a virtual factory that processes every method in an interface class without requiring
         * an implementation of the extension type.
         * The extension framework will execute the method handlers and pass in a proxy object instead of an underlying instance.
         * <br></br>
         * When this flag is present, the [ExtensionFactory.attach] method will never be called.
         */
        NON_VIRTUAL_FACTORY,

        /**
         * Do not wrap the backing object methods into [ExtensionHandler] instances and return a
         * [java.lang.reflect.Proxy] instance but return it as is. This allows the factory to
         * suport class objects as well as interfaces.
         * <br></br>
         * This is a corner use case and should normally not be used by any standard extension.
         * <br></br>
         * Legacy extension factories that need every method on an interface forwarded to the underlying implementation class
         * can set this flag to bypass the proxy logic of the extension framework.
         */
        DONT_USE_PROXY
    }

    /**
     * Returns true if the factory can process the given extension type.
     *
     * @param extensionType the extension type
     * @return whether the factory can produce an extension of the given type
     */
    fun accepts(extensionType: Class<*>?): Boolean

    /**
     * Attaches an extension type. This method is not called if [.getFactoryFlags] contains [FactoryFlag.NON_VIRTUAL_FACTORY].
     *
     * @param extensionType  The extension type
     * @param handleSupplier Supplies the database handle. This supplier may lazily open a Handle on the first
     * invocation. Extension implementors should take care not to fetch the handle before it is
     * needed, to avoid opening handles unnecessarily
     * @param <E>            the extension type
     * @return An extension of the given type, attached to the given handle
     * @throws IllegalArgumentException if the extension type is not supported by this factory
     * @see org.jdbi.v3.core.Jdbi.onDemand
    </E> */
    fun <E> attach(extensionType: Class<E>?, handleSupplier: HandleSupplier?): E {
        throw UnsupportedOperationException("Virtual factories do not support attach()")
    }

    val factoryFlags: Set<FactoryFlag?>?
        /**
         * Returns a set of [FactoryFlag]s that describe the extension factory.
         *
         * @return A set of [FactoryFlag] elements. Default is the empty set
         *
         * @since 3.38.0
         */
        get() = emptySet()
}