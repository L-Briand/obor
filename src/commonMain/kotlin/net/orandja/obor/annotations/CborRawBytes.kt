package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotate ByteArray, Array<Byte>, List<Byte> or any list like of bytes to serialize it with Major 2 (BYTE)
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class CborRawBytes