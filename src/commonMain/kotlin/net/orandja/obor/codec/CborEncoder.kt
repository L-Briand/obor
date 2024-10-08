package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.*
import net.orandja.obor.io.CborWriter
import net.orandja.obor.io.specific.ExpandableByteArray
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * Default Cbor encoder.
 *
 * @param writer Something that writes Cbor header and bytes.
 * @see CborWriter
 */
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalSerializationApi::class)
internal class CborEncoder(
    internal val writer: CborWriter,
    configuration: Cbor.Configuration,
) : Encoder, CompositeEncoder {

    override val serializersModule: SerializersModule = configuration.serializersModule

    // region Tracker

    companion object {
        // Metadata of each element inside the tracker array
        // @formatter:off
        private const val INDEFINITE     = 0b00000001.toByte() // Class annotated with @CborIndefinite `@CborIndefinite class Foo`
        private const val RAW_BYTES      = 0b00000010.toByte() // Class annotated with @CborRawBytes `@CborRawBytes class Foo`
        private const val STRUCTURE      = 0b00000100.toByte() // Class is a structure
        private const val INLINE         = 0b00001000.toByte() // Class is a value class
        private const val TAG_INDEFINITE = 0b00010000.toByte() // Class marked indefinite but not indefinite itself `class Foo(@CborIndefinite val bar: Bar); class Bar`
        private const val TUPLE          = 0b00100000.toByte() // Class becomes an ordered list.
        private const val INLINE_TUPLE   = 0b01000000.toByte() // Tuple class is inlined in a list and has `@CborTuple.inlinedInList`. The list becomes a list of tuples.
        // @formatter:on
    }

    /**
     * Contains the metadata annotations of all the previous elements in the hierarchy
     * If you have `class A(val b: B)`
     * - The class A contains annotations
     * - The field b contains annotations
     * - The class B contains annotations
     *
     * So the tracker have:
     * - When dealing with b the tracker contains `[metadata A, metadata b]`
     * - When dealing with B the tracker contains `[metadata A, metadata b, metadata B]`
     */
    private var tracker: ByteArray = ByteArray(16)

    /**
     * The parser needs to have access to at least element `n - 2` so the initial value should be >= 2
     */
    private var depth: Int = 2

    private inline fun ensureCapacity(elementsToAppend: Int) {
        if (depth + 1 + elementsToAppend <= tracker.size) return
        val newArray = ByteArray((tracker.size + 1 + elementsToAppend).takeHighestOneBit() shl 1)
        tracker.copyInto(newArray)
        tracker = newArray
    }

    private inline fun hasFlag(index: Int, flag: Byte): Boolean = tracker[index] and flag > 0
    private inline fun setFlag(index: Int, flag: Byte) {
        tracker[index] = tracker[index] or flag
    }

    private inline fun clear(index: Int) {
        tracker[index] = 0
    }

    // endregion

    private var header: Byte = 0
    private var collectionSize: Int = -1

    private inline fun updateTrackerWithAnnotations(annotations: List<Annotation>) {
        for (annotation in annotations) when (annotation) {
            is CborTag -> writer.writeMajor64(MAJOR_TAG, annotation.tag)
            is CborIndefinite -> setFlag(depth, INDEFINITE)
            is CborRawBytes -> setFlag(depth, RAW_BYTES)
            is CborTuple -> setFlag(depth, TUPLE)
        }
    }

    private inline fun startCollection(descriptor: SerialDescriptor) {
        for (i in 0 until descriptor.elementsCount) {
            if (descriptor.getElementAnnotations(i).any { it is CborSkip }) collectionSize--
        }
        if (hasFlag(depth, INLINE_TUPLE)) writer.writeMajor32(
            header, collectionSize * descriptor.getElementDescriptor(0).elementsCount
        )
        else writer.writeMajor32(header, collectionSize)
    }


    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        updateTrackerWithAnnotations(descriptor.annotations)
        this.collectionSize = collectionSize

        if (!hasFlag(depth, INLINE)) {
            ensureCapacity(1)
            depth++
        }

        if (hasFlag(depth, INDEFINITE)) {
            setFlag(depth, TAG_INDEFINITE)
            this.collectionSize = -1
        }

        when (descriptor.kind) {
            is StructureKind.LIST -> {
                if (hasFlag(depth - 1, INDEFINITE)) {
                    this.collectionSize = -1
                    setFlag(depth, TAG_INDEFINITE)
                }

                when {
                    // we check for metadata on depth - 1 because it might be something like class Foo(@CborIndefinite val bytes: List<Byte>)
                    descriptor is ByteArrayDescriptor -> header = MAJOR_BYTE
                    // Custom deserializer for indefinite Byte string
                    descriptor is ListBytesDescriptor -> {
                        header = MAJOR_BYTE
                    }

                    // Custom deserializer for indefinite string
                    descriptor is ListStringsDescriptor -> {
                        header = MAJOR_TEXT
                    }

                    hasFlag(depth - 1, RAW_BYTES) || hasFlag(depth, RAW_BYTES) -> {
                        if (descriptor.getElementDescriptor(0).kind !is PrimitiveKind.BYTE) throw CborEncoderException(
                            "${CborRawBytes::class.simpleName} annotation applied on an invalid field. It should be a List<Byte> or equivalent"
                        )
                        header = MAJOR_BYTE
                    }

                    else -> {
                        // If the sole element of the list is A CborTuple with inlinedInList == true
                        // We need to accommodate for the number of elements in the list.
                        if (descriptor.elementsCount == 1) {
                            for (annotation in descriptor.getElementDescriptor(0).annotations) if (annotation is CborTuple) {
                                if (annotation.inlinedInList) setFlag(depth, INLINE_TUPLE)
                                break
                            }
                        }
                        header = MAJOR_ARRAY
                    }
                }
            }

            is StructureKind.MAP -> header = MAJOR_MAP

            else -> throw CborEncoderException("Try to encode collection but SerialDescriptor (Descriptor: ${descriptor}, Kind: ${descriptor.kind}) isn't a StructureKind")
        }

        if (this.collectionSize < 0) writer.write(header or SIZE_INDEFINITE)
        else startCollection(descriptor)

        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        updateTrackerWithAnnotations(descriptor.annotations)

        if (!hasFlag(depth, INLINE)) {
            ensureCapacity(1)
            depth++
        }

        if (descriptor.kind !is StructureKind.CLASS && descriptor.kind !is StructureKind.OBJECT) throw CborEncoderException(
            "Try to encode collection but SerialDescriptor (Descriptor: ${descriptor}, Kind: ${descriptor.kind}) != StructureKind.CLASS or StructureKind.OBJECT"
        )

        header = if (hasFlag(depth - 1, TUPLE)) MAJOR_ARRAY else MAJOR_MAP
        setFlag(depth, STRUCTURE)

        // Don't write the size of an inlined tuple.
        if (hasFlag(depth - 2, INLINE_TUPLE)) return this

        // we check for metadata on depth - 1 because it might be something like class Foo(@CborIndefinite val bytes: MyClass)
        if (hasFlag(depth - 1, INDEFINITE)) {
            setFlag(depth, TAG_INDEFINITE)
            writer.write(header or SIZE_INDEFINITE)
        } else {
            collectionSize = descriptor.elementsCount
            startCollection(descriptor)
        }
        return this
    }


    override fun endStructure(descriptor: SerialDescriptor) {
        val indefinite = hasFlag(depth, TAG_INDEFINITE or INDEFINITE)
        if (hasFlag(depth - 1, RAW_BYTES)) flush(indefinite)

        // We don't write the size of an inlined tuple.
        if (indefinite && !hasFlag(depth - 1, INLINE_TUPLE)) writer.write(HEADER_BREAK)

        clear(depth)
        depth--
    }

    // region Composite

    // The default implementation of AbstractEncoder does not provide enough freedom to encode things correctly.
    // Compositing is omitted because the tracker array exists. With a single instance of CborEncoder, everything is much faster.

    private fun shouldEncodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (index < descriptor.elementsCount) for (annotation in descriptor.getElementAnnotations(index)) if (annotation is CborSkip) return false
        return true
    }

    private inline fun encodeElement(descriptor: SerialDescriptor, index: Int, block: () -> Unit) {
        if (hasFlag(depth, STRUCTURE) && !hasFlag(depth - 1, TUPLE) && descriptor.elementsCount > index)
            encodeRawString(descriptor.getElementName(index).encodeToByteArray())

        ensureCapacity(1)
        depth++
        if (descriptor.elementsCount > index)
            updateTrackerWithAnnotations(descriptor.getElementAnnotations(index))
        block()
        clear(depth)
        depth--
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeBoolean(value) }
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeByte(value) }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeShort(value) }
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeInt(value) }
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeLong(value) }
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeFloat(value) }
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeDouble(value) }
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeChar(value) }
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) { encodeString(value) }
    }

    override fun <T : Any?> encodeSerializableElement(
        descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T
    ) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) {
            serializer.serialize(
                this, value
            )
        }
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?
    ) {
        if (shouldEncodeElement(descriptor, index)) encodeElement(descriptor, index) {
            encodeNullableSerializableValue(
                serializer, value
            )
        }
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        if (shouldEncodeElement(descriptor, index)) encodeInline(descriptor.getElementDescriptor(index))
        else CborEncoderNoOp

    // region PRIMITIVE ENCODING

    /**
     * An inlined class will have the class and the value annotation.
     * For example:
     * ```kotlin
     * @A value class B(@C val d: D)
     * ```
     * In this case `d` is both `@A` and `@C`
     */
    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        updateTrackerWithAnnotations(descriptor.annotations)
        updateTrackerWithAnnotations(descriptor.getElementAnnotations(0))
        setFlag(depth, INLINE) // Tell beginStructure and beginCollection to skip depth increment.
        return this
    }

    private val buffer = ExpandableByteArray(0)

    private inline fun flush(indefinite: Boolean) {
        if (!indefinite || buffer.size == 0) return
        writer.writeMajor32(MAJOR_BYTE, buffer.size)
        writer.write(buffer.getSizedArray(), 0, buffer.size)
        buffer.size = 0
    }

    override fun encodeByte(value: Byte) {
        // @CborRawByte val data: ByteArray
        if (hasFlag(depth - 2, RAW_BYTES)) {
            // @CborIndefinite @CborRawByte val data: ByteArray
            // Cbor.encodeToByteArray(CborByteArraySerializer, data)
            if (hasFlag(depth - 2, INDEFINITE) || hasFlag(depth - 1, INDEFINITE)) {
                buffer.write(value)
                if (buffer.size == 255) flush(true)
            } else {
                writer.write(value)
            }
        } else {
            if (value < 0) writer.writeMajor8(MAJOR_NEGATIVE, value xor BYTE_FF)
            else writer.writeMajor8(MAJOR_POSITIVE, value)
        }
    }

    fun encodeUByte(value: UByte) {
        writer.writeMajor8(MAJOR_POSITIVE, value.toByte())
    }

    fun encodeNegativeUByte(value: UByte) {
        writer.writeMajor8(MAJOR_NEGATIVE, value.toByte())
    }

    override fun encodeBoolean(value: Boolean) {
        if (value) writer.write(HEADER_TRUE)
        else writer.write(HEADER_FALSE)
    }

    override fun encodeShort(value: Short) {
        if (value < 0) writer.writeMajor16(MAJOR_NEGATIVE, value xor SHORT_FF)
        else writer.writeMajor16(MAJOR_POSITIVE, value)
    }

    fun encodeUShort(value: UShort) {
        writer.writeMajor16(MAJOR_POSITIVE, value.toShort())
    }

    fun encodeNegativeUShort(value: UShort) {
        writer.writeMajor16(MAJOR_NEGATIVE, value.toShort())
    }

    override fun encodeInt(value: Int) {
        if (value < 0) writer.writeMajor32(MAJOR_NEGATIVE, value xor INT_FF)
        else writer.writeMajor32(MAJOR_POSITIVE, value)
    }

    fun encodeUInt(value: UInt) {
        writer.writeMajor32(MAJOR_POSITIVE, value.toInt())
    }

    fun encodeNegativeUInt(value: UInt) {
        writer.writeMajor32(MAJOR_NEGATIVE, value.toInt())
    }

    override fun encodeLong(value: Long) {
        if (value < 0) writer.writeMajor64(MAJOR_NEGATIVE, value xor LONG_FF)
        else writer.writeMajor64(MAJOR_POSITIVE, value)
    }

    fun encodeULong(value: ULong) {
        writer.writeMajor64(MAJOR_POSITIVE, value.toLong())
    }

    fun encodeNegativeULong(value: ULong) {
        writer.writeMajor64(MAJOR_NEGATIVE, value.toLong())
    }

    override fun encodeFloat(value: Float) {
        val float16Bits = float32ToFloat16bits(value)
        // NaN != NaN. toRawBits is necessary
        if (float16BitsToFloat32(float16Bits).toRawBits() == value.toRawBits()) {
            writer.writeHeader16(HEADER_FLOAT_16, float16Bits.toShort())
        } else {
            writer.writeHeader32(HEADER_FLOAT_32, value.toRawBits())
        }
    }

    override fun encodeDouble(value: Double) {
        val floatValue = float64toFloat32(value)
        // NaN != NaN. toRawBits is necessary
        if (floatValue.toDouble().toRawBits() == value.toRawBits()) encodeFloat(floatValue)
        else writer.writeHeader64(HEADER_FLOAT_64, value.toRawBits())
    }

    override fun encodeNull() = writer.write(HEADER_NULL)
    fun encodeUndefined() = writer.write(HEADER_UNDEFINED)

    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeString(value: String) {
        if (hasFlag(depth - 1, INDEFINITE) || hasFlag(depth, INDEFINITE)) encodeStringIndefinite(value)
        else encodeRawString(value.encodeToByteArray())
    }

    private fun encodeStringIndefinite(value: String) {
        val descriptor = ListStringsDescriptor(name(CborEncoder::class))
        encodeCollection(descriptor, if (value.length % 255 == 0) value.length / 255 else (value.length / 255) + 1) {
            for (item in value.chunkedSequence(255)) {
                // Force raw encoding instead of using encodeStringElement.
                // If not, it can result in indefinite string encoded in indefinite string
                encodeRawString(item.encodeToByteArray())
            }
        }
    }

    fun encodeRawString(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_TEXT, count)
        if (count != 0) writer.write(value, offset, count)
    }

    fun encodeBytesElement(serialDescriptor: SerialDescriptor, index: Int, value: ByteArray) = encodeBytes(value)
    fun encodeBytes(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_BYTE, count)
        if (count != 0) writer.write(value, offset, count)
    }

    // TODO : Enums as ints ?
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    // endregion
}