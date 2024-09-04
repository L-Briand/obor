package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.*
import net.orandja.obor.io.ByteWriter
import net.orandja.obor.io.CborWriter
import net.orandja.obor.io.specific.CborWriterExpandableByteArray
import net.orandja.obor.io.specific.ExpandableByteArray
import kotlin.experimental.and

@Serializable(CborObject.Serializer::class)
sealed class CborObject(val kind: Kind) {

    enum class Kind {
        POSITIVE, NEGATIVE, FLOAT, BOOLEAN, NULL, UNDEFINED, BYTES, BYTES_INDEFINITE, TEXT, TEXT_INDEFINITE, ARRAY, MAP, MAP_ENTRY, TAGGED,
    }

    /**
     * The companion object is the builder.
     * It can easily create CborObject of any kind.
     */
    companion object : CborObjectBuilder()

    /**
     * Write the CborObject without using [Cbor]
     * CBOR object know of itself
     */
    abstract fun writeInto(writer: CborWriter)

    /**
     * The minimum total amount of bytes the [CborObject] takes when serialized.
     */
    abstract val cborSize: Long

    /** The bytes representing the object */
    val cbor: ByteArray
        get() {
            val bytes = ByteArray(cborSize.toInt())
            val writer = CborWriter.ByWriter(ByteWriter.Of(bytes))
            writeInto(writer)
            return bytes
        }

    @OptIn(ExperimentalStdlibApi::class)
    val cborAsHexString: String get() = cbor.toHexString(HexFormat.UpperCase)

    // Description

    internal abstract fun describe(
        depth: Int,
        elements: MutableList<Description>,
        withWriter: (CborWriter.() -> Unit) -> ByteArray,
    )

    class Description(val depth: Int, val cborPart: ByteArray, val meaning: String)

    /**
     * Get descriptive information of the current CborObject.
     */
    fun getObjectDescription(): List<Description> {
        val descriptions = mutableListOf<Description>()
        val writerArray = ExpandableByteArray(64)
        val writer = CborWriterExpandableByteArray(writerArray)
        describe(0, descriptions) { it: CborWriter.() -> Unit ->
            writer.apply(it)
            val result = writerArray.getSizedArray()
            writerArray.size = 0
            result
        }
        return descriptions
    }

    /** The toString Method should display the content of CBOR with help information. */
    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        val descriptions = getObjectDescription()
        val leftMaxSize = descriptions.maxOf { (it.depth * 3) + (it.cborPart.size * 2) }
        return buildString {
            for (description in descriptions) {
                append(" ".repeat(description.depth * 3)) // padding
                append(description.cborPart.toHexString(HexFormat.UpperCase)) // cbor as hex string
                append(" ".repeat(leftMaxSize - description.depth * 3 - (description.cborPart.size * 2))) // padding
                append(" # ") // separator
                append(description.meaning) // meaning
                appendLine()
            }
        }
    }

    // Serializer

    internal object Serializer : KSerializer<CborObject> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = DummyDescriptor(
            serialName = name(CborObject::class)
        )

        override fun deserialize(decoder: Decoder): CborObject {
            decoder.assertCborDecoder { name(CborObject::class) }
            val peek = decoder.reader.peek()
            val major = peek and MAJOR_MASK
            return when (major) {
                MAJOR_POSITIVE -> CborPositive.Serializer.deserialize(decoder)
                MAJOR_NEGATIVE -> CborNegative.Serializer.deserialize(decoder)
                MAJOR_PRIMITIVE -> {
                    when (peek) {
                        HEADER_FALSE, HEADER_TRUE -> CborBoolean.Serializer.deserialize(decoder)
                        HEADER_NULL -> CborNull.Serializer.deserialize(decoder)
                        HEADER_UNDEFINED -> CborUndefined.Serializer.deserialize(decoder)
                        HEADER_FLOAT_64, HEADER_FLOAT_32, HEADER_FLOAT_16 -> CborFloat.Serializer.deserialize(decoder)

                        else -> throw CborDecoderException.FailedToDecodeElement(
                            decoder.reader.totalRead(), "PRIMITIVE Major 7 (float, boolean, null, undefined)"
                        )
                    }
                }

                MAJOR_BYTE -> {
                    if (peek and SIZE_MASK == SIZE_INDEFINITE) CborBytesIndefinite.Serializer.deserialize(decoder)
                    else CborBytes.Serializer.deserialize(decoder)
                }

                MAJOR_TEXT -> {
                    if (peek and SIZE_MASK == SIZE_INDEFINITE) CborTextIndefinite.Serializer.deserialize(decoder)
                    else CborText.Serializer.deserialize(decoder)
                }

                MAJOR_ARRAY -> CborArray.Serializer.deserialize(decoder)
                MAJOR_MAP -> CborMap.Serializer.deserialize(decoder)
                MAJOR_TAG -> CborTagged.Serializer.deserialize(decoder)

                else -> error("unreachable ${decoder.reader.totalRead()}")
            }
        }

        override fun serialize(encoder: Encoder, value: CborObject) {
            encoder.assertCborEncoder { name(CborObject::class) }
            when (value) {
                is CborArray -> CborArray.Serializer.serialize(encoder, value)
                is CborBoolean -> CborBoolean.Serializer.serialize(encoder, value)
                is CborBytes -> CborBytes.Serializer.serialize(encoder, value)
                is CborBytesIndefinite -> CborBytesIndefinite.Serializer.serialize(encoder, value)
                is CborFloat -> CborFloat.Serializer.serialize(encoder, value)
                is CborMap -> CborMap.Serializer.serialize(encoder, value)
                is CborNegative -> CborNegative.Serializer.serialize(encoder, value)
                is CborNull -> CborNull.Serializer.serialize(encoder, value)
                is CborPositive -> CborPositive.Serializer.serialize(encoder, value)
                is CborTagged -> CborTagged.Serializer.serialize(encoder, value)
                is CborText -> CborText.Serializer.serialize(encoder, value)
                is CborTextIndefinite -> CborTextIndefinite.Serializer.serialize(encoder, value)
                is CborUndefined -> CborUndefined.Serializer.serialize(encoder, value)
                is CborMapEntry -> CborMapEntry.Serializer.serialize(encoder, value)
            }
        }
    }

    // Utils

    protected fun sizeOfMajor(value: Long) = when {
        value < SIZE_8 -> 1L
        value <= 0xFFL -> 2L
        value <= 0xFFFFL -> 3L
        value <= 0xFFFF_FFFFL -> 5L
        else -> 9L
    }

    protected fun sizeOfMajor(value: Int) = when {
        value < SIZE_8 -> 1L
        value <= 0xFFL -> 2L
        value <= 0xFFFFL -> 3L
        else -> 5L
    }
}
