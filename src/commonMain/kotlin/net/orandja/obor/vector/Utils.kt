package net.orandja.obor.vector

internal fun oob(message: String): Nothing = throw IndexOutOfBoundsException(message)