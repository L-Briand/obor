package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.HEADER_UNDEFINED
import net.orandja.obor.codec.assertCborDecoder
import net.orandja.obor.codec.assertCborEncoder
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborUndefined.Serializer::class)
data object CborUndefined : CborObject(Kind.UNDEFINED) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        writer.write(HEADER_UNDEFINED)
    }

    override val cborSize: Long = 1

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {
        val cborPart = withWriter { write(HEADER_UNDEFINED) }
        elements += Description(depth, cborPart, "undefined")
    }

    @OptIn(ExperimentalSerializationApi::class)
    internal object Serializer : KSerializer<CborUndefined> {
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborUndefined::class)
        )

        override fun deserialize(decoder: Decoder): CborUndefined {
            decoder.assertCborDecoder { name(CborUndefined::class) }
            decoder.decodeUndefined()
            return CborUndefined
        }

        override fun serialize(encoder: Encoder, value: CborUndefined) {
            encoder.assertCborEncoder { name(CborUndefined::class) }
            encoder.encodeUndefined()
        }
    }
}