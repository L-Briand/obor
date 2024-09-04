package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.MAJOR_POSITIVE
import net.orandja.obor.codec.assertCborDecoder
import net.orandja.obor.codec.assertCborEncoder
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborPositive.Serializer::class)
data class CborPositive(val value: ULong) : CborObject(Kind.POSITIVE) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.writeMajor64(MAJOR_POSITIVE, value.toLong())
    }

    override val cborSize: Long = sizeOfMajor(value.toLong())

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cbor = withWriter { writeMajor64(MAJOR_POSITIVE, value.toLong()) }
        elements += Description(depth, cbor, "unsigned($value)")
    }

    internal object Serializer : KSerializer<CborPositive> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborPositive::class)
        )

        override fun deserialize(decoder: Decoder): CborPositive {
            decoder.assertCborDecoder { name(CborPositive::class) }
            return CborPositive(decoder.decodeULong())
        }

        override fun serialize(encoder: Encoder, value: CborPositive) {
            encoder.assertCborEncoder { name(CborPositive::class) }
            encoder.encodeULong(value.value)
        }
    }

}