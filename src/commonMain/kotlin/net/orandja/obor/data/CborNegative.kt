package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.*
import net.orandja.obor.io.CborWriter

@Serializable(CborNegative.Serializer::class)
data class CborNegative(val value: ULong) : CborObject(Kind.NEGATIVE) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.writeMajor64(MAJOR_NEGATIVE, value.toLong())
    }

    override val cborSize: Long = sizeOfMajor(value.toLong())

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cbor = withWriter { writeMajor64(MAJOR_NEGATIVE, value.toLong()) }
        elements += Description(depth, cbor, "negative($value)")
    }

    internal object Serializer : KSerializer<CborNegative> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborNegative::class)
        )

        override fun deserialize(decoder: Decoder): CborNegative {
            decoder.assertCborDecoder { name(CborNegative::class) }
            return CborNegative(decoder.decodeNegativeULong())
        }

        override fun serialize(encoder: Encoder, value: CborNegative) {
            encoder.assertCborEncoder { name(CborNegative::class) }
            encoder.encodeNegativeULong(value.value)
        }
    }
}