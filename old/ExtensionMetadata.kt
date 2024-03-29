//package com.github.danielchemko.winmdj.util
//
//import com.sun.org.apache.xalan.internal.lib.Extensions
//import java.util.Arrays
//import java.util.Collections
//import java.util.concurrent.Callable
//import java.util.function.Consumer
//import java.util.function.Function
//import java.util.function.Predicate
//import java.util.stream.Collectors
//
//
//class ExtensionMetadata private constructor(
//    private val extensionType: Class<*>,
//    instanceConfigCustomizer: ConfigCustomizer,
//    methodConfigCustomizers: Map<Method, ConfigCustomizer?>,
//    methodHandlers: Map<Method, ExtensionHandler>,
//    finalizer: Optional<Method>
//) {
//    private val instanceConfigCustomizer: ConfigCustomizer = instanceConfigCustomizer
//    private val methodConfigCustomizers: Map<Method, ConfigCustomizer?>
//    private val methodHandlers: Map<Method, ExtensionHandler>
//    private val finalizer: Optional<Method>
//
//    init {
//        this.methodConfigCustomizers = Collections.unmodifiableMap(methodConfigCustomizers)
//        this.methodHandlers = Collections.unmodifiableMap(methodHandlers)
//        this.finalizer = finalizer
//    }
//
//    fun extensionType(): Class<*> {
//        return extensionType
//    }
//
//    /**
//     * Create an instance specific configuration based on all instance customizers. The instance configuration holds all
//     * custom configuration that was applied e.g. through instance annotations.
//     *
//     * @param config A configuration object. The object is not changed
//     * @return A new configuration object with all changes applied
//     */
//    fun createInstanceConfiguration(config: ConfigRegistry): ConfigRegistry {
//        val instanceConfiguration: ConfigRegistry = config.createCopy()
//        instanceConfigCustomizer.customize(instanceConfiguration)
//        return instanceConfiguration
//    }
//
//    /**
//     * Create an method specific configuration based on all method customizers. The method configuration holds all
//     * custom configuration that was applied e.g. through method annotations.
//     *
//     * @param method The method that is about to be called
//     * @param config A configuration object. The object is not changed
//     * @return A new configuration object with all changes applied
//     */
//    fun createMethodConfiguration(method: Method, config: ConfigRegistry): ConfigRegistry {
//        val methodConfiguration: ConfigRegistry = config.createCopy()
//        val methodConfigCustomizer: ConfigCustomizer? = methodConfigCustomizers[method]
//        if (methodConfigCustomizer != null) {
//            methodConfigCustomizer.customize(methodConfiguration)
//        }
//        return methodConfiguration
//    }
//
//    val extensionMethods: Set<Any>
//        /**
//         * Returns a set of all Methods that have [ExtensionHandler] objects associated with them.
//         */
//        get() = methodHandlers.keys
//
//    /**
//     * Returns a reference to a method that overrides [Object.finalize] if it exists.
//     * @return An [Optional] containing a [Method] if a finalizer exists.
//     */
//    fun getFinalizer(): Optional<Method> {
//        return finalizer
//    }
//
//    /**
//     * Creates an [ExtensionHandlerInvoker] instance for a specific method.
//     * @param target The target object on which the invoker should work
//     * @param method The method which will trigger the invocation
//     * @param handleSupplier A [HandleSupplier] that will provide the handle object for the extension method
//     * @param config The configuration object which should be used as base for the method specific configuration
//     * @return A [ExtensionHandlerInvoker] object that is linked to the method
//     * @param <E> THe type of the target object
//    </E> */
//    fun <E> createExtensionHandlerInvoker(
//        target: E, method: Method,
//        handleSupplier: HandleSupplier, config: ConfigRegistry
//    ): ExtensionHandlerInvoker {
//        return ExtensionHandlerInvoker(target, method, methodHandlers[method], handleSupplier, config)
//    }
//
//    /**
//     * Builder class for the [ExtensionMetadata] object.
//     * See [ExtensionMetadata.builder].
//     */
//    class Builder internal constructor(
//        /**
//         * Returns the extension type from the builder.
//         * @return The extension type
//         */
//        val extensionType: Class<*>
//    ) {
//        private val extensionHandlerFactories: MutableCollection<ExtensionHandlerFactory> = ArrayList()
//        private val extensionHandlerCustomizers: MutableCollection<ExtensionHandlerCustomizer> =
//            ArrayList<ExtensionHandlerCustomizer>()
//        private val configCustomizerFactories: MutableCollection<ConfigCustomizerFactory> =
//            ArrayList<ConfigCustomizerFactory>()
//
//        private val instanceConfigCustomizer: ConfigCustomizerChain = ConfigCustomizerChain()
//        private val methodConfigCustomizers: MutableMap<Method, ConfigCustomizerChain?> =
//            HashMap<Method, ConfigCustomizerChain?>()
//        private val methodHandlers: MutableMap<Method, ExtensionHandler> = HashMap<Method, ExtensionHandler>()
//
//        private val extensionTypeMethods: MutableCollection<Method> = HashSet<Method>()
//
//        private val finalizer: Optional<Method>
//
//        init {
//            extensionTypeMethods.addAll(Arrays.asList(*extensionType.methods))
//            extensionTypeMethods.addAll(Arrays.asList(*extensionType.declaredMethods))
//
//            extensionTypeMethods.stream()
//                .filter(Predicate<Method> { m: Method -> !m.isSynthetic() })
//                .collect(Collectors.groupingBy<Any, Any>(Function<Any, Any> { m: Any ->
//                    Arrays.asList(
//                        m.getName(),
//                        Arrays.asList(m.getParameterTypes())
//                    )
//                }))
//                .values()
//                .stream()
//                .filter { methodCount -> methodCount.size() > 1 }
//                .findAny()
//                .ifPresent { methods ->
//                    throw UnableToCreateExtensionException(
//                        "%s has ambiguous methods (%s) found, please resolve with an explicit override",
//                        extensionType, methods
//                    )
//                }
//
//            this.finalizer = JdbiClassUtils.safeMethodLookup(extensionType, "finalize")
//        }
//
//        /**
//         * Adds an [ExtensionHandlerFactory] that will be used to find extension handlers when the [Builder.build]} method is called.
//         * @param extensionHandlerFactory An [ExtensionHandlerFactory] instance
//         * @return The builder instance
//         */
//        fun addExtensionHandlerFactory(extensionHandlerFactory: ExtensionHandlerFactory): Builder {
//            extensionHandlerFactories.add(extensionHandlerFactory)
//            return this
//        }
//
//        /**
//         * Adds an [ExtensionHandlerCustomizer] that will be used to customize extension handlers when the [Builder.build]} method is called.
//         * @param extensionHandlerCustomizer An [ExtensionHandlerCustomizer] instance
//         * @return The builder instance
//         */
//        fun addExtensionHandlerCustomizer(extensionHandlerCustomizer: ExtensionHandlerCustomizer): Builder {
//            extensionHandlerCustomizers.add(extensionHandlerCustomizer)
//            return this
//        }
//
//        /**
//         * Adds an [ConfigCustomizerFactory] that will be used to find configuration customizers when the [Builder.build]} method is called.
//         * @param configCustomizerFactory An [ConfigCustomizerFactory] instance
//         * @return The builder instance
//         */
//        fun addConfigCustomizerFactory(configCustomizerFactory: ConfigCustomizerFactory): Builder {
//            configCustomizerFactories.add(configCustomizerFactory)
//            return this
//        }
//
//        /**
//         * Add an instance specific configuration customizer. This customizer will be applied to all methods on the extension type.
//         * @param configCustomizer A [ConfigCustomizer]
//         * @return The builder instance
//         */
//        fun addInstanceConfigCustomizer(configCustomizer: ConfigCustomizer?): Builder {
//            instanceConfigCustomizer.addCustomizer(configCustomizer)
//            return this
//        }
//
//        /**
//         * Add a method specific configuration customizer. This customizer will be applied only to the method given here.
//         * @param method A method object
//         * @param configCustomizer A [ConfigCustomizer]
//         * @return The builder instance
//         */
//        fun addMethodConfigCustomizer(method: Method?, configCustomizer: ConfigCustomizer?): Builder {
//            val methodConfigCustomizer: ConfigCustomizerChain = methodConfigCustomizers.computeIfAbsent(method,
//                Function<Method, ConfigCustomizerChain> { m: Method? -> ConfigCustomizerChain() })
//            methodConfigCustomizer.addCustomizer(configCustomizer)
//            return this
//        }
//
//        /**
//         * Adds a new extension handler for a method.
//         *
//         * @param method The method for which an extension handler should be registered.
//         * @param handler An [ExtensionHandler] instance
//         * @return The builder instance
//         */
//        fun addMethodHandler(method: Method, handler: ExtensionHandler): Builder {
//            methodHandlers[method] = handler
//            return this
//        }
//
//        /**
//         * Creates a new [ExtensionMetadata] object.
//         *
//         * @return A [ExtensionMetadata] object
//         */
//        fun build(): ExtensionMetadata {
//            // add all methods that are declared on the extension type and
//            // are not static and don't already have a handler
//
//            val seen: MutableSet<Method> = HashSet<Any?>(methodHandlers.keys)
//            for (method in extensionTypeMethods) {
//                // skip static methods and methods that already have method handlers
//                if (Modifier.isStatic(method.getModifiers()) || !seen.add(method)) {
//                    continue
//                }
//
//                // look through the registered extension handler factories to find extension handlers
//                var handler: ExtensionHandler = findExtensionHandlerFor(extensionType, method)
//                    .orElseGet { ExtensionHandler.missingExtensionHandler(method) }
//
//
//                // apply extension handler customizers
//                for (extensionHandlerCustomizer in extensionHandlerCustomizers) {
//                    handler = extensionHandlerCustomizer.customize(handler, extensionType, method)
//                }
//
//                methodHandlers[method] = handler
//            }
//
//            configCustomizerFactories.forEach(Consumer<ConfigCustomizerFactory> { configCustomizerFactory: ConfigCustomizerFactory ->
//                configCustomizerFactory.forExtensionType(
//                    extensionType
//                )
//                    .forEach { configCustomizer: ConfigCustomizer? ->
//                        this.addInstanceConfigCustomizer(
//                            configCustomizer
//                        )
//                    }
//            })
//
//            for (method in methodHandlers.keys) {
//                // call all method configurer factories.
//                configCustomizerFactories.forEach(Consumer<ConfigCustomizerFactory> { configCustomizerFactory: ConfigCustomizerFactory ->
//                    configCustomizerFactory.forExtensionMethod(
//                        extensionType, method
//                    )
//                        .forEach { configCustomizer ->
//                            this.addMethodConfigCustomizer(
//                                method,
//                                configCustomizer
//                            )
//                        }
//                })
//            }
//
//            return ExtensionMetadata(
//                extensionType,
//                instanceConfigCustomizer,
//                methodConfigCustomizers,
//                methodHandlers,
//                finalizer
//            )
//        }
//
//        private fun findExtensionHandlerFor(extensionType: Class<*>, method: Method): Optional<ExtensionHandler>? {
//            for (extensionHandlerFactory in extensionHandlerFactories) {
//                if (extensionHandlerFactory.accepts(extensionType, method)) {
//                    val result: Optional<ExtensionHandler>? =
//                        extensionHandlerFactory.createExtensionHandler(extensionType, method)
//                    if (result.isPresent()) {
//                        return result
//                    }
//                }
//            }
//            return Optional.empty()
//        }
//    }
//
//    /**
//     * Wraps all config customizers and the handler for a specific method execution.
//     * An invoker is created using [ExtensionMetadata.createExtensionHandlerInvoker].
//     */
//    inner class ExtensionHandlerInvoker internal constructor(
//        private val target: Any,
//        method: Method,
//        extensionHandler: ExtensionHandler?,
//        private val handleSupplier: HandleSupplier,
//        config: ConfigRegistry
//    ) {
//        private val extensionContext: ExtensionContext
//        private val extensionHandler: ExtensionHandler?
//
//        init {
//            val methodConfig: ConfigRegistry = createMethodConfiguration(method, config)
//            this.extensionContext = ExtensionContext.forExtensionMethod(
//                methodConfig,
//                extensionType, method
//            )
//
//            this.extensionHandler = extensionHandler
//
//            try {
//                this.extensionHandler.warm(methodConfig)
//            } catch (e: Exception) {
//                // if fail fast is requested, fail right at warmup time.
//                if (config.get(Extensions::class.java).isFailFast()) {
//                    throw UnableToCreateExtensionException(e, "While inspecting %s: %s", method, e.message)
//                }
//            }
//        }
//
//        /**
//         * Invoke the registered extension handler code in the extension context. The
//         * extension context wraps the method that gets executed and a full customized configuration
//         * (both instance and method specific configuration customizers have been applied). The
//         * extension context is registered with the underlying handle to configure the handle when
//         * executing the registered [ExtensionHandler].
//         *
//         * @param args The arguments to pass into the extension handler
//         * @return The result of the extension handler invocation
//         */
//        fun invoke(vararg args: Any?): Any {
//            val handlerArgs: Array<Any> = JdbiClassUtils.safeVarargs(args)
//            val callable =
//                Callable { extensionHandler!!.invoke(handleSupplier, target, *handlerArgs) }
//            return call(callable)
//        }
//
//        /**
//         * Invoke a callable in the extension context. The extension context wraps the method that
//         * gets executed and a full customized configuration (both instance and method specific
//         * configuration customizers have been applied). The extension context is registered with
//         * the underlying handle to configure the handle when calling the [Callable.call] method.
//         * <br></br>
//         * This method is used by the generated classes from the `jdbi3-generator` annotation
//         * processor to execute predefined [ExtensionHandler] instances.
//         *
//         * @param callable The callable to use
//         * @return The result of the extension handler invocation
//         */
//        fun call(callable: Callable<*>?): Any {
//            try {
//                return handleSupplier.invokeInContext(extensionContext, callable)
//            } catch (x: Exception) {
//                throw Sneaky.throwAnyway(x)
//            }
//        }
//
//        /**
//         * Invoke a runnable in the extension context. The extension context wraps the method that
//         * gets executed and a full customized configuration (both instance and method specific
//         * configuration customizers have been applied). The extension context is registered with
//         * the underlying handle to configure the handle when calling the [Runnable.run] method.
//         * <br></br>
//         * This method is used by the generated classes from the `jdbi3-generator` annotation
//         * processor to execute predefined [ExtensionHandler] instances.
//         *
//         * @param runnable The runnable to use
//         */
//        fun call(runnable: Runnable) {
//            call(Callable<Any?> {
//                runnable.run()
//                null
//            })
//        }
//    }
//
//    companion object {
//        /**
//         * Returns a new [ExtensionMetadata.Builder] instance.
//         * @param extensionType The extension type for which metadata is collected
//         * @return A new [ExtensionMetadata.Builder] instance
//         */
//        fun builder(extensionType: Class<*>): Builder {
//            return Builder(extensionType)
//        }
//    }
//}