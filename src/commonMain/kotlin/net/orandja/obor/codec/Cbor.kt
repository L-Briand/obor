@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.orandja.obor.codec

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.orandja.obor.data.CborObject
import net.orandja.obor.io.ByteWriter
import net.orandja.obor.io.CborReader
import net.orandja.obor.io.CborWriter
import net.orandja.obor.io.specific.CborReaderByteArray
import net.orandja.obor.io.specific.CborWriterExpandableByteArray
import net.orandja.obor.io.specific.ExpandableByteArray
import kotlin.jvm.JvmStatic

/**
 * The main entry point to serialize / deserialize CBOR messages.
 */
open class Cbor private constructor(
    private val configuration: Configuration
) : BinaryFormat {

    override val serializersModule: SerializersModule = configuration.serializersModule

    /**
     * Decodes an object from the provided CBOR [reader] using the specified deserialization strategy.
     *
     * @param T The type of the object to be decoded.
     * @param deserializer The deserialization strategy for the object.
     * @param reader The [CborReader] from which bytes are read.
     * @return The decoded object.
     */
    fun <T> decodeFromReader(deserializer: DeserializationStrategy<T>, reader: CborReader): T {
        return deserializer.deserialize(CborDecoder(reader, configuration))
    }

    /**
     * Encodes an object to the provided CBOR [writer] using the specified serialization strategy.
     *
     * @param T The type of the object to be encoded.
     * @param serializer The serialization strategy for the object.
     * @param value The object to be encoded.
     * @param writer The [CborWriter] to which bytes are written.
     *
     * @see ByteWriter
     * @see CborWriter
     */
    fun <T> encodeToWriter(serializer: SerializationStrategy<T>, value: T, writer: CborWriter) {
        serializer.serialize(CborEncoder(writer, configuration), value)
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
        decodeFromReader(deserializer, CborReaderByteArray(bytes))

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        // It's faster to encode CborObject without normal serializer.
        if (value is CborObject && serializer.descriptor == CborObject.Serializer.descriptor)
            return value.cbor

        // Normal encoding
        val out = ExpandableByteArray()
        encodeToWriter(serializer, value, CborWriterExpandableByteArray(out))
        return out.getSizedArray()
    }

    fun <T> decodeFromCborObject(deserializer: DeserializationStrategy<T>, value: CborObject): T =
        decodeFromByteArray(deserializer, encodeToByteArray(serializersModule.serializer(), value))

    // Configuration

    /** Configuration clas used by CBOR's encoder and decoder */
    data class Configuration(
        /** Set to true to ignore unknown keys during deserialization */
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

    fun copy(builder: Configuration.Builder.() -> Unit): Cbor =
        Cbor(Configuration.Builder(configuration).apply(builder).build())

    companion object : Cbor(Configuration()) {
        @JvmStatic
        operator fun invoke(builder: Configuration.Builder.() -> Unit) =
            Cbor(Configuration.Builder((this as Cbor).configuration).apply(builder).build())
    }

}