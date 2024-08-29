@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.orandja.obor.codec

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.io.*
import kotlin.jvm.JvmStatic

open class Cbor private constructor(
    private val configuration: Configuration
) : BinaryFormat {
    override val serializersModule: SerializersModule = configuration.serializersModule

    fun <T> decodeFromReader(deserializer: DeserializationStrategy<T>, reader: CborReader): T {
        return deserializer.deserialize(CborDecoder(reader, configuration))
    }

    fun <T> encodeToWriter(serializer: SerializationStrategy<T>, value: T, writer: CborWriter) {
        serializer.serialize(CborEncoder(writer, configuration), value)
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
        decodeFromReader(deserializer, CborReaderByteArray(bytes))

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val out = ExpandableByteArray()
        encodeToWriter(serializer, value, CborWriterExpandableByteArray(out))
        return out.getSizedArray()
    }

    // Config

    fun copy(builder: Configuration.Builder.() -> Unit): Cbor =
        Cbor(Configuration.Builder(configuration).apply(builder).build())

    data class Configuration(
        val ignoreUnknownKeys: Boolean = false,
        val serializersModule: SerializersModule = EmptySerializersModule()
    ) {
        class Builder internal constructor(private val from: Configuration?) {
            var serializersModule: SerializersModule? = null
            var ingnoreUnknownKeys: Boolean? = null

            internal fun build() = Configuration(
                ignoreUnknownKeys = ingnoreUnknownKeys ?: from?.ignoreUnknownKeys
                ?: (Companion as Cbor).configuration.ignoreUnknownKeys,
                serializersModule = serializersModule ?: from?.serializersModule
                ?: (Companion as Cbor).configuration.serializersModule,
            )
        }
    }

    companion object : Cbor(Configuration()) {
        @JvmStatic
        operator fun invoke(builder: Configuration.Builder.() -> Unit) =
            Cbor(Configuration.Builder((this as Cbor).configuration).apply(builder).build())
    }

}