@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.orandja.obor.codec

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.decoder.CborDecoder
import net.orandja.obor.codec.encoder.CborEncoder
import net.orandja.obor.codec.reader.CborInputStreamReader
import net.orandja.obor.codec.reader.CborUByteReader
import net.orandja.obor.codec.writer.CborOutputStreamWriter
import net.orandja.obor.codec.writer.CborUByteWriter
import net.orandja.obor.vector.UByteVector
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
@ExperimentalSerializationApi
@InternalSerializationApi
class Cbor(override val serializersModule: SerializersModule = EmptySerializersModule) : BinaryFormat {

    companion object Default : BinaryFormat by Cbor()

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        return deserializer.deserialize(CborDecoder(CborUByteReader(bytes.asUByteArray()), serializersModule))
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val out = UByteVector()
        serializer.serialize(CborEncoder(CborUByteWriter(out), serializersModule), value)
        return out.nativeArray.asByteArray()
    }

    fun <T> decodeFromInputStream(deserializer: DeserializationStrategy<T>, input: InputStream): T {
        return deserializer.deserialize(CborDecoder(CborInputStreamReader(input), serializersModule))
    }

    fun <T> encodeToOutputStream(serializer: SerializationStrategy<T>, value: T, output: OutputStream) {
        serializer.serialize(CborEncoder(CborOutputStreamWriter(output), serializersModule), value)
    }
}

