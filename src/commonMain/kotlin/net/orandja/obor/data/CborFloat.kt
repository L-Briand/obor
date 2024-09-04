package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.*
import net.orandja.obor.io.CborWriter

@Serializable(CborFloat.Serializer::class)
data class CborFloat(val value: Double) : CborObject(Kind.FLOAT) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        encodeDouble(writer, value)
    }

    override val cborSize: Long = cborSize().toLong()

    private fun cborSize(): Int {
        val floatValue = float64toFloat32(value)
        return if (floatValue.toDouble().toRawBits() == value.toRawBits()) {
            val float16Bits = float32ToFloat16bits(floatValue)
            if (float16BitsToFloat32(float16Bits).toRawBits() == floatValue.toRawBits()) 3 else 5
        } else 7
    }

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cbor = withWriter { encodeDouble(this, value) }
        val meaning = when (cbor.size) {
            3 -> "float16($value)"
            5 -> "float32($value)"
            9 -> "float64($value)"
            else -> error("invalid size for float: ${cbor.size}")
        }
        elements += Description(depth, cbor, meaning)
    }

    private fun encodeFloat(writer: CborWriter, value: Float) {
        val float16Bits = float32ToFloat16bits(value)
        // NaN != NaN. toRawBits is necessary
        if (float16BitsToFloat32(float16Bits).toRawBits() == value.toRawBits()) {
            writer.writeHeader16(HEADER_FLOAT_16, float16Bits.toShort())
        } else {
            writer.writeHeader32(HEADER_FLOAT_32, value.toRawBits())
        }
    }

    private fun encodeDouble(writer: CborWriter, value: Double) {
        val floatValue = float64toFloat32(value)
        // NaN != NaN. toRawBits is necessary
        if (floatValue.toDouble().toRawBits() == value.toRawBits()) encodeFloat(writer, floatValue)
        else writer.writeHeader64(HEADER_FLOAT_64, value.toRawBits())
    }

    internal object Serializer : KSerializer<CborFloat> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborFloat::class)
        )

        override fun deserialize(decoder: Decoder): CborFloat {
            return CborFloat(decoder.decodeDouble())
        }

        override fun serialize(encoder: Encoder, value: CborFloat) {
            encoder.encodeDouble(value.value)
        }
    }
}