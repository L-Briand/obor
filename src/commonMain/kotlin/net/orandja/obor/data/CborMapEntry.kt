package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.name
import net.orandja.obor.io.CborWriter

@Serializable(CborMapEntry.Serializer::class)
data class CborMapEntry(val key: CborObject, val value: CborObject) : CborObject(Kind.MAP_ENTRY) {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        key.writeInto(writer)
        value.writeInto(writer)
    }

    override val cborSize: Long = key.cborSize + value.cborSize

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {
        key.describe(depth, elements, withWriter)
        value.describe(depth, elements, withWriter)
    }

    internal object Serializer : KSerializer<CborMapEntry> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborMapEntry::class)
        )

        override fun deserialize(decoder: Decoder): CborMapEntry = CborMapEntry(
            CborObject.Serializer.deserialize(decoder),
            CborObject.Serializer.deserialize(decoder),
        )

        override fun serialize(encoder: Encoder, value: CborMapEntry) {
            CborObject.Serializer.serialize(encoder, value.key)
            CborObject.Serializer.serialize(encoder, value.value)
        }
    }
}