package com.github.danielchemko.winmdj.util

class UnableToCreateExtensionException(message: String?, throwable: Throwable?) : RuntimeException(message, throwable) {
    constructor(cause: Throwable?) : this(null, cause)

    constructor(message: String) : this(message, null)
}