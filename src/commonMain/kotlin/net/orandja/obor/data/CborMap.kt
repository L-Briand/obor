package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.*
import net.orandja.obor.io.CborWriter
import kotlin.experimental.or

@Serializable(CborMap.Serializer::class)
data class CborMap(val elements: MutableList<CborMapEntry>, val indefinite: Boolean) : CborObject(Kind.MAP),
    MutableList<CborMapEntry> by elements {

    val asMap: Map<CborObject, CborObject> get() = CborMapAsMap()

    inner class CborMapAsMap : Map<CborObject, CborObject> {

        private inner class MapEntry(delegate: CborMapEntry) : Map.Entry<CborObject, CborObject> {
            override val key: CborObject = delegate.key
            override val value: CborObject = delegate.value
        }

        override val entries: Set<Map.Entry<CborObject, CborObject>> = elements.map(::MapEntry).toSet()
        override val keys: Set<CborObject> = elements.map { it.key }.toSet()
        override val size: Int = elements.size
        override val values: Collection<CborObject> = elements.map { it.value }
        override fun isEmpty(): Boolean = elements.isEmpty()
        override fun get(key: CborObject): CborObject? = elements.find { it.key == key }?.value
        override fun containsValue(value: CborObject): Boolean = elements.any { it.value == value }
        override fun containsKey(key: CborObject): Boolean = elements.any { it.key == key }
    }

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        if (indefinite) writer.write(MAJOR_MAP or SIZE_INDEFINITE)
        else writer.writeMajor32(MAJOR_MAP, elements.size)
        for (it in elements) it.writeInto(writer)
        if (indefinite) writer.write(HEADER_BREAK)
    }

    override val cborSize: Long = elements.sumOf { it.cborSize } + (if (indefinite) 2 else sizeOfMajor(elements.size))

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {
        if (indefinite) {
            val cborPart = withWriter { write(MAJOR_MAP or SIZE_INDEFINITE) }
            elements += Description(depth, cborPart, "map(*)")
        } else {
            val cborPart = withWriter { writeMajor32(MAJOR_MAP, this@CborMap.elements.size) }
            elements += Description(depth, cborPart, "map(${this.elements.size})")
        }
        this.elements.onEach { it.describe(depth + 1, elements, withWriter) }
        if (indefinite) {
            val cborPart = withWriter { write(HEADER_BREAK) }
            elements += Description(depth, cborPart, "break")
        }
    }

    internal object Serializer : KSerializer<CborMap> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborMap::class)
        )

        override fun deserialize(decoder: Decoder): CborMap {
            decoder.assertCborDecoder { name(CborMap::class) }
            val result = mutableListOf<CborMapEntry>()
            if (!(decoder.reader.peek() hasMajor MAJOR_MAP)) throw CborDecoderException.FailedToDecodeElement(
                decoder.reader.totalRead(), "PRIMITIVE Major 5 MAP"
            )
            val size = decoder.decodeCollectionSize()
            if (size == -1) {
                while (decoder.reader.peek() != HEADER_BREAK) result += CborMapEntry.Serializer.deserialize(decoder)
                decoder.reader.consume()
            } else {
                for (i in 0 until size) result += CborMapEntry.Serializer.deserialize(decoder)
            }
            return CborMap(result, size == -1)
        }

        override fun serialize(encoder: Encoder, value: CborMap) {
            encoder.assertCborEncoder { name(CborMap::class) }
            if (value.indefinite) encoder.writer.write(MAJOR_MAP or SIZE_INDEFINITE)
            else encoder.writer.writeMajor32(MAJOR_MAP, value.elements.size)
            for (it in value.elements) CborMapEntry.Serializer.serialize(encoder, it)
            if (value.indefinite) encoder.writer.write(HEADER_BREAK)
        }
    }

}