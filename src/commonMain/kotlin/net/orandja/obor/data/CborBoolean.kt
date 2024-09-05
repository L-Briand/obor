package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.HEADER_FALSE
import net.orandja.obor.codec.HEADER_TRUE
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborBoolean.Serializer::class)
data class CborBoolean(val value: Boolean) : CborObject(Kind.BOOLEAN) {

    override fun toString(): String = super.toString()

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cbor = withWriter { write(if (value) HEADER_TRUE else HEADER_FALSE) }
        elements += Description(depth, cbor, value.toString())
    }

    override fun writeInto(writer: CborWriter) {
        writer.write(if (value) HEADER_TRUE else HEADER_FALSE)
    }

    override val cborSize: Long = 1

    internal object Serializer : KSerializer<CborBoolean> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborBoolean::class)
        )

        override fun deserialize(decoder: Decoder): CborBoolean {
            return CborBoolean(decoder.decodeBoolean())
        }

        override fun serialize(encoder: Encoder, value: CborBoolean) {
            encoder.encodeBoolean(value.value)
        }
    }
}