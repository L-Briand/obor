package net.orandja.obor

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import net.orandja.obor.codec.Cbor
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
inline fun <reified T> assertTransformation(expected: ByteArray, serializer: KSerializer<T>, data: T) {
    assertEncode(expected, serializer, data)
    assertDecode(expected, serializer, data)
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
inline fun <reified T> assertEncode(expected: ByteArray, serializer: KSerializer<T>, data: T) {
    val encoded = Cbor.encodeToByteArray(serializer, data)
    assertContentEquals(expected, encoded)
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
inline fun <reified T> assertDecode(expected: ByteArray, serializer: KSerializer<T>, data: T) {
    val decoded = Cbor.decodeFromByteArray(serializer, expected )
    assertEquals(data, decoded)
}