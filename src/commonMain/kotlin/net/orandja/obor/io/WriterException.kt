package net.orandja.obor.io

/**
 * This exception should be thrown if something wrong occurs in a [NativeArrayWriter]
 */
open class WriterException(message: String? = null, throwable: Throwable? = null) : Exception(message, throwable)