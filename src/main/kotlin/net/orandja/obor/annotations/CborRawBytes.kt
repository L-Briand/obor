package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotate a ByteArray, Array<Byte> or List<Byte> to serialize it with Major 2 (BYTE)
 */
@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class CborRawBytes