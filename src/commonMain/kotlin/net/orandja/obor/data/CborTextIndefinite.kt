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

@Serializable(CborTextIndefinite.Serializer::class)
data class CborTextIndefinite(val value: MutableList<CborText>) : CborObject(Kind.TEXT_INDEFINITE),
    MutableList<CborText> by value {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.write(MAJOR_TEXT or SIZE_INDEFINITE)
        for (element in value) element.writeInto(writer)
        writer.write(HEADER_BREAK)
    }

    override val cborSize: Long = value.sumOf { it.cborSize } + 2

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    ) {
        val cborHeader = withWriter { write(MAJOR_TEXT or SIZE_INDEFINITE) }
        elements += Description(depth, cborHeader, "text(*)")
        for (element in value) element.describe(depth + 1, elements, withWriter)
        elements += Description(depth, withWriter { write(HEADER_BREAK) }, "break")
    }

    internal object Serializer : KSerializer<CborTextIndefinite> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborTextIndefinite::class)
        )

        override fun deserialize(decoder: Decoder): CborTextIndefinite {
            decoder.assertCborDecoder { name(CborTextIndefinite::class) }
            val reader = decoder.reader
            if (reader.peek() != (MAJOR_TEXT or SIZE_INDEFINITE)) throw FailedToDecodeElement(
                reader.totalRead(),
                "INDEFINITE BYTEARRAY (Major: $MAJOR_TEXT, Size: $SIZE_INDEFINITE)"
            )
            reader.consume()
            val result = mutableListOf<CborText>()
            while (reader.peek() != HEADER_BREAK) result += CborText.Serializer.deserialize(decoder)
            reader.consume()
            return CborTextIndefinite(result)
        }

        override fun serialize(encoder: Encoder, value: CborTextIndefinite) {
            encoder.assertCborEncoder { name(CborTextIndefinite::class) }
            encoder.writer.write(MAJOR_TEXT or SIZE_INDEFINITE)
            for (text in value.value) CborText.Serializer.serialize(encoder, text)
            encoder.writer.write(HEADER_BREAK)
        }
    }
}