package net.orandja.obor.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CborArrayStringSerializer : KSerializer<Array<String>> {
    override val descriptor: SerialDescriptor = CborListStringSerializer.descriptor
    override fun deserialize(decoder: Decoder): Array<String> =
        CborListStringSerializer.deserialize(decoder).toTypedArray()

    override fun serialize(encoder: Encoder, value: Array<String>) =
        CborListStringSerializer.serialize(encoder, value.toList())
}