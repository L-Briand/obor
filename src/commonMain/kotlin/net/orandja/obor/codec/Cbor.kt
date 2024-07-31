@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.orandja.obor.codec

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.decoder.CborDecoder
import net.orandja.obor.codec.encoder.CborEncoder
import net.orandja.obor.io.ByteVector
import net.orandja.obor.io.CborByteReader
import net.orandja.obor.io.CborByteWriter

@OptIn(InternalSerializationApi::class, ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class)
class Cbor(override val serializersModule: SerializersModule = EmptySerializersModule()) : BinaryFormat {

    companion object Default : BinaryFormat by Cbor()

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        return deserializer.deserialize(CborDecoder(CborByteReader(bytes), serializersModule))
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val out = ByteVector()
        serializer.serialize(CborEncoder(CborByteWriter(out), serializersModule), value)
        return out.nativeArray
    }
//
//    fun <T> decodeFromInputStream(deserializer: DeserializationStrategy<T>, input: InputStream): T {
//        return deserializer.deserialize(CborDecoder(CborInputStreamReader(input), serializersModule))
//    }
//
//    fun <T> encodeToOutputStream(serializer: SerializationStrategy<T>, value: T, output: OutputStream) {
//        serializer.serialize(CborEncoder(CborOutputStreamWriter(output), serializersModule), value)
//    }
}

