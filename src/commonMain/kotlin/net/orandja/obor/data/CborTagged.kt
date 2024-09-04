package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.*
import net.orandja.obor.codec.CborDecoderException.InvalidSizeElement
import net.orandja.obor.io.CborReader
import net.orandja.obor.io.CborWriter
import kotlin.experimental.and

@Serializable(CborTagged.Serializer::class)
data class CborTagged(val tag: Long, val value: CborObject) : CborObject(Kind.TAGGED) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.writeMajor64(MAJOR_TAG, tag)
        value.writeInto(writer)
    }

    override val cborSize: Long = sizeOfMajor(tag) + value.cborSize

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {
        val cborPart = withWriter { writeMajor64(MAJOR_TAG, tag) }
        elements += Description(depth, cborPart, "tag($tag)")
        value.describe(depth + 1, elements, withWriter)
    }

    internal object Serializer : KSerializer<CborTagged> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborTagged::class)
        )

        override fun deserialize(decoder: Decoder): CborTagged {
            decoder.assertCborDecoder { name(CborTagged::class) }
            if (!(decoder.reader.peek() hasMajor MAJOR_TAG)) throw CborDecoderException.FailedToDecodeElement(
                decoder.reader.totalRead(), "TAG (Major: $MAJOR_TAG)"
            )
            val tag = decodeTag(decoder.reader)
            val value = CborObject.Serializer.deserialize(decoder)
            return CborTagged(tag, value)
        }

        private fun decodeTag(reader: CborReader): Long {
            val sizeBits = reader.peekConsume() and SIZE_MASK
            return when {
                sizeBits < SIZE_8 -> sizeBits.toLong()
                sizeBits == SIZE_8 -> reader.nextByte().toLong() and 0xFF
                sizeBits == SIZE_16 -> reader.nextShort().toLong() and 0xFFFF
                sizeBits == SIZE_32 -> reader.nextInt().toLong() and 0xFFFFFFFF
                sizeBits == SIZE_64 -> reader.nextLong()
                else -> throw InvalidSizeElement(reader.totalRead(), sizeBits, SIZE_64, false)
            }
        }

        override fun serialize(encoder: Encoder, value: CborTagged) {
            encoder.assertCborEncoder { name(CborTagged::class) }
            encoder.writer.writeMajor64(MAJOR_TAG, value.tag)
            CborObject.Serializer.serialize(encoder, value.value)
        }
    }

}