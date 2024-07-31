package net.orandja.obor.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import net.orandja.obor.codec.*

/** The default behavior of the  */
@OptIn(ExperimentalSerializationApi::class)
object CborListStringSerializer : KSerializer<List<String>> {

    override val descriptor: SerialDescriptor = ListStringsDescriptor(name(CborListByteArraySerializer::class))

    override fun deserialize(decoder: Decoder): List<String> {
        decoder.assertCborDecoder { name(CborListStringSerializer::class) }
        val builder = mutableListOf<String>()
        decoder.decodeStructure(descriptor) {
            assertCborDecoder(::unreachable)
            var index: Int
            while (true) {
                index = decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                builder.add(decodeStringElement(descriptor.getElementDescriptor(index), index))
            }
        }
        return builder
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.assertCborEncoder { name(CborListStringSerializer::class) }
        encoder.encodeCollection(descriptor, -1) {
            assertCborEncoder(::unreachable)
            for ((index, bytes) in value.withIndex()) {
                encodeStringElement(CborListByteArraySerializer.descriptor.getElementDescriptor(index), index, bytes)
            }
        }
    }
}