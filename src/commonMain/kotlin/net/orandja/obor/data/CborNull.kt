package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.HEADER_NULL
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborNull.Serializer::class)
data object CborNull : CborObject(Kind.NULL) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.write(HEADER_NULL)
    }

    override val cborSize: Long = 1

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {
        val cborPart = withWriter { write(HEADER_NULL) }
        elements += Description(depth, cborPart, "null")
    }

    @OptIn(ExperimentalSerializationApi::class)
    internal object Serializer : KSerializer<CborNull> {
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborNull::class)
        )

        override fun deserialize(decoder: Decoder): CborNull {
            decoder.decodeNull()
            return CborNull
        }

        override fun serialize(encoder: Encoder, value: CborNull) {
            encoder.encodeNull()
        }
    }

}