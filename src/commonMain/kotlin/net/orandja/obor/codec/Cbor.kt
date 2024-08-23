@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.orandja.obor.codec

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.io.*

open class Cbor private constructor(override val serializersModule: SerializersModule = EmptySerializersModule()) :
    BinaryFormat {

    companion object : Cbor() {
        operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var serializersModule: SerializersModule = EmptySerializersModule()
        internal fun build() = Cbor(serializersModule)
    }


    fun <T> decodeFromReader(deserializer: DeserializationStrategy<T>, reader: CborReader): T {
        return deserializer.deserialize(CborDecoder(reader, serializersModule))
    }

    fun <T> encodeToWriter(serializer: SerializationStrategy<T>, value: T, writer: CborWriter) {
        serializer.serialize(CborEncoder(writer, serializersModule), value)
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
        decodeFromReader(deserializer, CborReaderByteArray(bytes))

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val out = ExpandableByteArray()
        encodeToWriter(serializer, value, CborWriterExpandableByteArray(out))
        return out.getSizedArray()
    }
}