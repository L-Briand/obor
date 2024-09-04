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

@Serializable(CborArray.Serializer::class)
data class CborArray(val elements: MutableList<CborObject>, val indefinite: Boolean) : CborObject(Kind.ARRAY),
    MutableList<CborObject> by elements {

    override fun toString(): String = super.toString()
    override fun writeInto(writer: CborWriter) {
        if (indefinite) writer.write(MAJOR_ARRAY or SIZE_INDEFINITE)
        else writer.writeMajor32(MAJOR_ARRAY, elements.size)
        for (it in elements) it.writeInto(writer)
        if (indefinite) writer.write(HEADER_BREAK)
    }

    override val cborSize: Long = elements.sumOf { it.cborSize } + if (indefinite) 2 else sizeOfMajor(elements.size)

    override fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray
    ) {
        if (indefinite) {
            val cborPart = withWriter { write(MAJOR_ARRAY or SIZE_INDEFINITE) }
            elements += Description(depth, cborPart, "array(*)")
        } else {
            val cborPart = withWriter { writeMajor32(MAJOR_ARRAY, this@CborArray.elements.size) }
            elements += Description(depth, cborPart, "array(${this.elements.size})")
        }
        this.elements.onEach { it.describe(depth + 1, elements, withWriter) }
        if (indefinite) {
            val cborPart = withWriter { write(HEADER_BREAK) }
            elements += Description(depth, cborPart, "break")
        }
    }

    internal object Serializer : KSerializer<CborArray> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborArray::class)
        )

        override fun deserialize(decoder: Decoder): CborArray {
            decoder.assertCborDecoder { name(CborArray::class) }
            val result = mutableListOf<CborObject>()
            if (!(decoder.reader.peek() hasMajor MAJOR_ARRAY)) throw CborDecoderException.FailedToDecodeElement(
                decoder.reader.totalRead(), "PRIMITIVE Major 4 ARRAY"
            )
            val size = decoder.decodeCollectionSize()
            if (size == -1) {
                while (decoder.reader.peek() != HEADER_BREAK) result += CborObject.Serializer.deserialize(decoder)
                decoder.reader.consume()
            } else {
                for (i in 0 until size) result += CborObject.Serializer.deserialize(decoder)
            }
            return CborArray(result, size == -1)
        }

        override fun serialize(encoder: Encoder, value: CborArray) {
            encoder.assertCborEncoder { name(CborArray::class) }
            if (value.indefinite) encoder.writer.write(MAJOR_ARRAY or SIZE_INDEFINITE)
            else encoder.writer.writeMajor32(MAJOR_ARRAY, value.elements.size)
            for (it in value.elements) CborObject.Serializer.serialize(encoder, it)
            if (value.indefinite) encoder.writer.write(HEADER_BREAK)
        }
    }
}