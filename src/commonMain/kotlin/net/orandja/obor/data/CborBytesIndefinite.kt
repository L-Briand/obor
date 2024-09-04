package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.*
import net.orandja.obor.codec.CborDecoderException.FailedToDecodeElement
import net.orandja.obor.io.CborWriter
import kotlin.experimental.or

@Serializable(CborBytesIndefinite.Serializer::class)
data class CborBytesIndefinite(val elements: MutableList<CborBytes>) : CborObject(Kind.BYTES_INDEFINITE),
    MutableList<CborBytes> by elements {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.write(MAJOR_BYTE or SIZE_INDEFINITE)
        for (element in this.elements) element.writeInto(writer)
        writer.write(HEADER_BREAK)
    }

    override val cborSize: Long = elements.sumOf { it.cborSize } + 2

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cborHeader = withWriter { write(MAJOR_BYTE or SIZE_INDEFINITE) }
        elements += Description(depth, cborHeader, "bytes(*)")
        for (element in this.elements) element.describe(depth + 1, elements, withWriter)
        elements += Description(depth, withWriter { write(HEADER_BREAK) }, "break")
    }

    internal object Serializer : KSerializer<CborBytesIndefinite> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborBytesIndefinite::class)
        )

        override fun deserialize(decoder: Decoder): CborBytesIndefinite {
            decoder.assertCborDecoder { name(CborBytesIndefinite::class) }
            val reader = decoder.reader
            if (reader.peek() != (MAJOR_BYTE or SIZE_INDEFINITE)) throw FailedToDecodeElement(
                reader.totalRead(),
                "INDEFINITE BYTEARRAY (Major: $MAJOR_BYTE, Size: $SIZE_INDEFINITE)"
            )
            reader.consume()
            val result = mutableListOf<CborBytes>()
            while (reader.peek() != HEADER_BREAK) result += CborBytes.Serializer.deserialize(decoder)
            reader.consume()
            return CborBytesIndefinite(result)
        }

        override fun serialize(encoder: Encoder, value: CborBytesIndefinite) {
            encoder.assertCborEncoder { name(CborBytesIndefinite::class) }
            encoder.writer.write(MAJOR_BYTE or SIZE_INDEFINITE)
            for (bytes in value.elements) CborBytes.Serializer.serialize(encoder, bytes)
            encoder.writer.write(HEADER_BREAK)
        }
    }
}