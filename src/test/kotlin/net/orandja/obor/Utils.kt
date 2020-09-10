package net.orandja.obor

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import net.orandja.obor.codec.Cbor
import org.junit.jupiter.api.Assertions

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
fun <T> assertTransformation(expected: ByteArray, serializer: KSerializer<T>, data: T) {
    assertEncode(expected, serializer, data)
    assertDecode(expected, serializer, data)
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
fun <T> assertEncode(expected: ByteArray, serializer: KSerializer<T>, data: T) {
    val encoded = Cbor.encodeToByteArray(serializer, data)
    Assertions.assertArrayEquals(expected, encoded)
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
fun <T> assertDecode(expected: ByteArray, serializer: KSerializer<T>, data: T) {
    val decoded = Cbor.decodeFromByteArray(serializer, expected)
    Assertions.assertEquals(data, decoded)
}