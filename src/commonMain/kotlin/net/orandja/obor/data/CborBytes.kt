package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.codec.assertCborDecoder
import net.orandja.obor.codec.assertCborEncoder
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborBytes.Serializer::class)
data class CborBytes(val value: ByteArray) : CborObject(Kind.BYTES) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.writeMajor32(MAJOR_BYTE, value.size)
        writer.write(value)
    }

    override val cborSize: Long = value.size.toLong() + sizeOfMajor(value.size)

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cborHeader = withWriter { writeMajor32(MAJOR_BYTE, value.size) }
        elements += Description(depth, cborHeader, "bytes(${value.size})")
        elements += Description(depth + 1, value, buildString {
            append('"')
            for (byte in value) {
                val char = Char(byte.toUByte().toInt())
                append(
                    if (char.isISOControl() || char.isDefined()) "\\u${char.code.toString().padStart(4, '0')}"
                    else char.toString()
                )
            }
            append('"')
        })
    }

    internal object Serializer : KSerializer<CborBytes> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborBytes::class)
        )

        override fun deserialize(decoder: Decoder): CborBytes {
            decoder.assertCborDecoder { name(CborBytes::class) }
            return CborBytes(decoder.decodeBytes())
        }

        override fun serialize(encoder: Encoder, value: CborBytes) {
            encoder.assertCborEncoder { name(CborBytes::class) }
            encoder.encodeBytes(value.value)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CborBytes) return false

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}