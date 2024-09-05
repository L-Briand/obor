package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.MAJOR_TEXT
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborText.Serializer::class)
data class CborText(val value: String) : CborObject(Kind.TEXT) {
    private val bytes by lazy { value.encodeToByteArray() }

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.writeMajor32(MAJOR_TEXT, bytes.size)
        writer.write(bytes)
    }

    override val cborSize: Long by lazy { sizeOfMajor(bytes.size) + bytes.size }
    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {

        val cborHeader = withWriter { writeMajor32(MAJOR_TEXT, value.length) }
        elements += Description(depth, cborHeader, "text(${value.length})")
        elements += Description(depth + 1, bytes, buildString {
            append('"')
            append(value)
            append('"')
        })
    }

    internal object Serializer : KSerializer<CborText> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborText::class)
        )

        override fun deserialize(decoder: Decoder): CborText {
            return CborText(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: CborText) {
            encoder.encodeString(value.value)
        }
    }
}