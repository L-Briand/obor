package net.orandja.obor.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import net.orandja.obor.codec.*

/** The default behavior of the  */
@OptIn(ExperimentalSerializationApi::class)
object CborListByteArraySerializer : KSerializer<List<ByteArray>> {

    override val descriptor: SerialDescriptor = ListBytesDescriptor(name(CborListByteArraySerializer::class))

    override fun deserialize(decoder: Decoder): List<ByteArray> {
        decoder.assertCborDecoder { name(CborListByteArraySerializer::class) }
        val builder = mutableListOf<ByteArray>()
        decoder.decodeStructure(descriptor) {
            assertCborDecoder(::unreachable)
            var index: Int
            while (true) {
                index = decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                builder.add(decodeBytesElement(descriptor.getElementDescriptor(index), index))
            }
        }
        return builder
    }

    override fun serialize(encoder: Encoder, value: List<ByteArray>) {
        encoder.assertCborEncoder { name(CborListByteArraySerializer::class) }
        encoder.encodeCollection(descriptor, value.size) {
            assertCborEncoder(::unreachable)
            for ((index, bytes) in value.withIndex()) {
                encodeBytesElement(descriptor.getElementDescriptor(index), index, bytes)
            }
        }
    }
}