//package com.github.danielchemko.winmdj.util
//
//import com.sun.org.apache.xalan.internal.lib.Extensions
//import java.util.*
//import java.util.stream.Collectors
//
//
//internal class ExtensionFactoryDelegate(delegatedFactory: ExtensionFactory) : ExtensionFactory {
//    private val delegatedFactory: ExtensionFactory = delegatedFactory
//
//    fun accepts(extensionType: Class<*>?): Boolean {
//        return delegatedFactory.accepts(extensionType)
//    }
//
//    fun getDelegatedFactory(): ExtensionFactory {
//        return delegatedFactory
//    }
//
//    fun getExtensionHandlerFactories(config: ConfigRegistry?): Collection<ExtensionHandlerFactory> {
//        return Collections.unmodifiableCollection(
//            delegatedFactory.getExtensionHandlerFactories(config).stream()
//                .map(FilteringExtensionHandlerFactory::forDelegate)
//                .collect(Collectors.toList())
//        )
//    }
//
//    fun getExtensionHandlerCustomizers(config: ConfigRegistry?): Collection<ExtensionHandlerCustomizer> {
//        return delegatedFactory.getExtensionHandlerCustomizers(config)
//    }
//
//    fun getConfigCustomizerFactories(config: ConfigRegistry?): Collection<ConfigCustomizerFactory> {
//        return delegatedFactory.getConfigCustomizerFactories(config)
//    }
//
//    fun buildExtensionMetadata(builder: Builder?) {
//        delegatedFactory.buildExtensionMetadata(builder)
//    }
//
//    val factoryFlags: Set<Any>
//        get() = delegatedFactory.getFactoryFlags()
//
//    fun <E> attach(extensionType: Class<E>?, handleSupplier: HandleSupplier): E {
//        val factoryFlags: Set<FactoryFlag> = factoryFlags
//
//        // If the extension declares that it supports classes, then the proxy logic
//        // in the delegate is bypassed. This code uses the Java proxy class which does not
//        // work for Classes (when extending a class, the assumption is that the returned
//        // object can be cast to the class itself, something that can not be done with a
//        // java proxy object).
//        //
//        // The extension factory is now responsible for managing the method invocations itself.
//        //
//        if (factoryFlags.contains(DONT_USE_PROXY)) {
//            return delegatedFactory.attach(extensionType, handleSupplier)
//        }
//
//        if (extensionType == null || !extensionType.isInterface) {
//            throw IllegalArgumentException(
//                format(
//                    "Can not attach %s as an extension with %s",
//                    extensionType, delegatedFactory.getClass().getSimpleName()
//                )
//            )
//        }
//
//        val config: ConfigRegistry = handleSupplier.getConfig()
//        val extensions: Extensions = config.get(Extensions::class.java)
//
//        extensions.onCreateProxy()
//
//        val extensionMetaData: ExtensionMetadata = extensions.findMetadata(extensionType, delegatedFactory)
//        val instanceConfig: ConfigRegistry = extensionMetaData.createInstanceConfiguration(config)
//
//        val handlers: MutableMap<Method, ExtensionHandlerInvoker> = HashMap<Method, ExtensionHandlerInvoker>()
//        val proxy: Any = Proxy.newProxyInstance(
//            extensionType.classLoader,
//            arrayOf<Class<*>>(extensionType)
//        ) { proxyInstance, method, args -> handlers[method].invoke(args) }
//
//        // if the object created by the delegated factory has actual methods (it is not delegating), attach the
//        // delegate and pass it to the handlers. Otherwise assume that there is no backing object and do not call
//        // attach.
//        val delegatedInstance = if (factoryFlags.contains(NON_VIRTUAL_FACTORY)) delegatedFactory.attach(
//            extensionType,
//            handleSupplier
//        ) else proxy
//
//        // add proxy specific methods (toString, equals, hashCode, finalize)
//        // those will only be added if they don't already exist in the method handler map.
//
//        // If these methods are added, they are special because they operate on the proxy object itself, not the underlying object
//        val toStringHandler =
//            ExtensionHandler { h: HandleSupplier?, target: Any?, args: Array<Any?>? ->
//                "Jdbi extension proxy for " + extensionType.name + "@" + Integer.toHexString(
//                    proxy.hashCode()
//                )
//            }
//        handlers[TOSTRING_METHOD] = extensionMetaData.ExtensionHandlerInvoker(
//            proxy,
//            TOSTRING_METHOD,
//            toStringHandler,
//            handleSupplier,
//            instanceConfig
//        )
//
//        handlers[EQUALS_METHOD] = extensionMetaData.ExtensionHandlerInvoker(
//            proxy,
//            EQUALS_METHOD,
//            EQUALS_HANDLER,
//            handleSupplier,
//            instanceConfig
//        )
//        handlers[HASHCODE_METHOD] = extensionMetaData.ExtensionHandlerInvoker(
//            proxy,
//            HASHCODE_METHOD,
//            HASHCODE_HANDLER,
//            handleSupplier,
//            instanceConfig
//        )
//
//        // add all methods that are delegated to the underlying object / existing handlers
//        extensionMetaData.getExtensionMethods().forEach { method ->
//            handlers.put(
//                method,
//                extensionMetaData.createExtensionHandlerInvoker(
//                    delegatedInstance,
//                    method,
//                    handleSupplier,
//                    instanceConfig
//                )
//            )
//        }
//
//        // finalize is double special. Add this unconditionally, even if subclasses try to override it.
//        extensionMetaData.getFinalizer().ifPresent { method ->
//            handlers.put(
//                method,
//                extensionMetaData.ExtensionHandlerInvoker(proxy, method, NULL_HANDLER, handleSupplier, instanceConfig)
//            )
//        }
//
//        return extensionType.cast(proxy)
//    }
//
//    override fun toString(): String {
//        return "ExtensionFactoryDelegate for " + delegatedFactory.toString()
//    }
//}