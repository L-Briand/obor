@file:OptIn(ExperimentalSerializationApi::class)

package net.orandja.obor.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.ByteArrayDescriptor
import net.orandja.obor.codec.assertCborDecoder
import net.orandja.obor.codec.assertCborEncoder
import net.orandja.obor.codec.name

/** The default behavior of the  */
object CborByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = ByteArrayDescriptor(name(CborByteArraySerializer::class))

    override fun deserialize(decoder: Decoder): ByteArray {
        decoder.assertCborDecoder { name(CborByteArraySerializer::class) }
        return decoder.decodeBytes()
    }

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.assertCborEncoder { name(CborByteArraySerializer::class) }
        encoder.encodeBytes(value, 0, value.size)
    }
}