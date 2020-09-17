package net.orandja.obor.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.decoder.CborDecoder
import net.orandja.obor.codec.encoder.CborEncoder

/**
 * Encode and decode any type
 * Note : ByteArray, Array<Byte>, List<Byte> are handled as Major 4 (LIST) not Major 2 (BYTE).
 */
@ExperimentalUnsignedTypes
@InternalSerializationApi
@ExperimentalSerializationApi
class AnySerializer : KSerializer<Any> {

    companion object Default : KSerializer<Any> by AnySerializer()

    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        "net.orandja.obor.AnySerializer",
        SerialKind.CONTEXTUAL
    )

    override fun deserialize(decoder: Decoder): Any {
        decoder as? CborDecoder ?: throw IllegalStateException("Decoder should be ${CborDecoder::class}")
        return decoder.decodeValue()
    }


    override fun serialize(encoder: Encoder, value: Any) {
        encoder as? CborEncoder ?: throw IllegalStateException("Encoder should be ${CborEncoder::class}")
        encoder.encodeValue(value)
    }
}