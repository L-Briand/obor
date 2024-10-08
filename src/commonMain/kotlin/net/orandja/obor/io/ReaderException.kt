package net.orandja.obor.io

/**
 * This exception should be thrown if something wrong occurs in a [NativeArrayReader]
 */
open class ReaderException(message: String? = null, throwable: Throwable? = null) : RuntimeException(message, throwable)