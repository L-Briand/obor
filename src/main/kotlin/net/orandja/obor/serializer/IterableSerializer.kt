package net.orandja.obor.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

/** Encode and decode any kind of collections */
@ExperimentalSerializationApi
@InternalSerializationApi
@Serializer(forClass = Iterable::class)
class IterableSerializer<T : Any?>(private val dataSerializer: KSerializer<T>) : KSerializer<Iterable<T>> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        "net.orandja.obor.IterableSerializer",
        StructureKind.LIST
    )

    private val listSerializer = ListSerializer(dataSerializer)
    override fun deserialize(decoder: Decoder): Iterable<T> = listSerializer.deserialize(decoder)
    override fun serialize(encoder: Encoder, value: Iterable<T>) {
        encoder.encodeStructure(descriptor) {
            value.forEach { dataSerializer.serialize(this as Encoder, it) }
        }
    }
}