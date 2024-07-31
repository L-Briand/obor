package net.orandja.obor.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CborArrayByteArraySerializer : KSerializer<Array<ByteArray>> {
    override val descriptor: SerialDescriptor = CborListByteArraySerializer.descriptor
    override fun deserialize(decoder: Decoder): Array<ByteArray> =
        CborListByteArraySerializer.deserialize(decoder).toTypedArray()

    override fun serialize(encoder: Encoder, value: Array<ByteArray>) =
        CborListByteArraySerializer.serialize(encoder, value.toList())
}