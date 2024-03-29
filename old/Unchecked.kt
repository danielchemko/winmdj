package com.github.danielchemko.winmdj.util

import java.io.IOException
import java.io.UncheckedIOException
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Callable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier
import javax.annotation.Nonnull


class Unchecked {

    interface CheckedRunnable {
        @Throws(Exception::class)
        fun run()
    }

    companion object {
        fun <T> consumer(checkedConsumer: CheckedConsumer<T>): Consumer<T> {
            return Consumer<T> { x ->
                try {
                    checkedConsumer.accept(x)
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }

        fun runnable(checkedRunnable: CheckedRunnable): Runnable {
            return Runnable {
                try {
                    checkedRunnable.run()
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }

        fun <T> callable(checkedCallable: CheckedCallable<T>): SneakyCallable<T> {
            return SneakyCallable<T> {
                try {
                    return@SneakyCallable checkedCallable.call()
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }

        fun <T> supplier(checkedSupplier: CheckedSupplier<T>): Supplier<T> {
            return Supplier<T> {
                try {
                    return@Supplier checkedSupplier.get()
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }

        fun <X, T> function(checkedFunction: CheckedFunction<X, T>): java.util.function.Function<X, T> {
            return java.util.function.Function<X, T> { x ->
                try {
                    return@Function checkedFunction.apply(x)
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }

        fun <X, Y, T> biFunction(checkedBiFunction: CheckedBiFunction<X, Y, T>): BiFunction<X, Y, T> {
            return BiFunction<X, Y, T> { x: X, y: Y ->
                try {
                    return@BiFunction checkedBiFunction.apply(x, y)
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }

        fun <X, Y> biConsumer(checkedBiConsumer: CheckedBiConsumer<X, Y>): BiConsumer<X, Y> {
            return BiConsumer<X, Y> { x: X, y: Y ->
                try {
                    checkedBiConsumer.accept(x, y)
                } catch (t: Throwable) {
                    throw Sneaky.throwAnyway(t)
                }
            }
        }
    }
}

fun interface CheckedBiConsumer<X, Y> {
    @Throws(Throwable::class)
    fun accept(x: X, y: Y)
}

fun interface CheckedBiFunction<X, Y, T> {
    @Throws(Throwable::class)
    fun apply(x: X, y: Y): T
}

fun interface CheckedCallable<T> {
    @Throws(Throwable::class)
    fun call(): T
}

fun interface CheckedConsumer<T> {
    @Throws(java.lang.Exception::class)
    fun accept(t: T)
}

fun interface CheckedFunction<X, T> {
    @Throws(Throwable::class)
    fun apply(x: X): T
}

fun interface CheckedSupplier<T> {
    @Throws(Throwable::class)
    fun get(): T
}

class Sneaky {
    companion object {
        /**
         * Will **always** throw an exception, so the caller should also always throw the dummy return value to make sure the control flow remains clear.
         */
        @Nonnull
        fun throwAnyway(t: Throwable): DummyException {
            if (t is Error) {
                throw t
            } else if (t is RuntimeException) {
                throw t
            } else if (t is IOException) {
                throw UncheckedIOException(t)
            } else if (t is InterruptedException) {
                Thread.currentThread().interrupt()
            } else if (t is InvocationTargetException) {
                throw throwAnyway(t.cause!!)
            }

            throw throwEvadingChecks<RuntimeException>(t)
        }

        private fun <E : Throwable> throwEvadingChecks(throwable: Throwable): E {
            throw throwable as E
        }
    }
}

class DummyException private constructor() : java.lang.RuntimeException() {
    init {
        throw UnsupportedOperationException("this exception should never actually be instantiated or thrown")
    }
}

fun interface SneakyCallable<T> : Callable<T> {
    override fun call(): T
}