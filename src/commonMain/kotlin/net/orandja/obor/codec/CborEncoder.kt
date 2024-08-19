package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.annotations.CborSkip
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.io.CborWriter
import net.orandja.obor.io.ExpandableArray
import net.orandja.obor.io.ExpandableByteArray
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * Default Cbor decoder.
 *
 * @param writer Something that writes Cbor header and bytes.
 * @see CborWriter
 */
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalSerializationApi::class)
internal open class CborEncoder(
    protected val writer: CborWriter,
    override val serializersModule: SerializersModule
) : Encoder, CompositeEncoder {

    // region Tracker

    companion object {
        // Metadata of each element inside the tracker array
        // @formatter:off
        private const val INFINITE     = 0b00001.toByte() // annotated with @CborInfinite
        private const val RAW_BYTES    = 0b00010.toByte() // annotated with @CborRawBytes
        private const val STRUCTURE    = 0b00100.toByte() // Class is a structure
        private const val INLINE       = 0b01000.toByte() // Class is value class
        private const val TAG_INFINITE = 0b10000.toByte() // Class marked infinite but not infinite itself
        // @formatter:on
    }

    /**
     * Contains the metadata annotations of all the previous elements in the hierarchy
     * If you have `class A(val b: B)`
     * - The class A contains annotations,
     * - the field b contains annotations and
     * - the class B contains annotations
     *
     * So the tracker have:
     * - When dealing with b the tracker contains `[metadata A, metadata b]`
     * - When dealing with B the tracker contains `[metadata A, metadata b, metadata B]`
     *
     * The tracker is a [ExpandableArray] in essence,
     * but it is more efficient to have it embedded in the class directly.
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
            is CborInfinite -> setFlag(depth, INFINITE)
            is CborRawBytes -> setFlag(depth, RAW_BYTES)
        }
    }

    private inline fun startCollection(descriptor: SerialDescriptor) {
        for (i in 0 until descriptor.elementsCount) {
            if (descriptor.getElementAnnotations(i).any { it is CborSkip }) collectionSize--
        }
        writer.writeMajor32(header, collectionSize)
    }


    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        updateTrackerWithAnnotations(descriptor.annotations)
        this.collectionSize = collectionSize

        if (!hasFlag(depth, INLINE)) {
            ensureCapacity(1)
            depth++
        }

        if (hasFlag(depth, INFINITE)) {
            setFlag(depth, TAG_INFINITE)
            this.collectionSize = -1
        }

        when (descriptor.kind) {
            is StructureKind.LIST -> when {
                // we check for metadata on depth - 1 because it might be something like class Foo(@CborInfinite val bytes: List<Byte>)
                descriptor is ByteArrayDescriptor -> header = MAJOR_BYTE
                // Custom deserializer for infinite Byte string
                descriptor is ListBytesDescriptor -> {
                    if (hasFlag(depth - 1, INFINITE)) {
                        this.collectionSize = -1
                        setFlag(depth, TAG_INFINITE)
                    }
                    header = MAJOR_BYTE
                }

                // Custom deserializer for infinite string
                descriptor is ListStringsDescriptor -> {
                    if (hasFlag(depth - 1, INFINITE)) {
                        this.collectionSize = -1
                        setFlag(depth, TAG_INFINITE)
                    }
                    header = MAJOR_TEXT
                }

                hasFlag(depth - 1, RAW_BYTES) || hasFlag(depth, RAW_BYTES) -> {
                    if (descriptor.getElementDescriptor(0).kind !is PrimitiveKind.BYTE)
                        error("${CborRawBytes::class.simpleName} annotation applied on an invalid field. It should be a List<Byte> or equivalent")
                    if (hasFlag(depth - 1, INFINITE)) {
                        this.collectionSize = -1
                        setFlag(depth, TAG_INFINITE)
                    }
                    header = MAJOR_BYTE
                }

                else -> header = MAJOR_ARRAY
            }

            is StructureKind.MAP -> header = MAJOR_MAP
            is StructureKind.CLASS, is StructureKind.OBJECT -> {
                header = MAJOR_MAP
                setFlag(depth, STRUCTURE)
            }

            else -> throw IllegalStateException("Try to encode a ${descriptor.kind} but SerialDescriptor isn't a StructureKind")
        }
        if (this.collectionSize < 0) writer.write(header or SIZE_INFINITE)
        else startCollection(descriptor)

        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        updateTrackerWithAnnotations(descriptor.annotations)

        if (!hasFlag(depth, INLINE)) {
            ensureCapacity(1)
            depth++
        }
        when (descriptor.kind) {
            is StructureKind.CLASS, is StructureKind.OBJECT -> {
                header = MAJOR_MAP
                setFlag(depth, STRUCTURE)
                // we check for metadata on depth - 1 because it might be something like class Foo(@CborInfinite val bytes: MyClass)
                if (hasFlag(depth - 1, INFINITE)) {
                    setFlag(depth, TAG_INFINITE)
                    writer.write(header or SIZE_INFINITE)
                } else {
                    collectionSize = descriptor.elementsCount
                    startCollection(descriptor)
                }
                return this
            }

            is StructureKind.LIST -> when {
                descriptor is ByteArrayDescriptor || descriptor is ListBytesDescriptor -> header = MAJOR_BYTE
                descriptor is ListStringsDescriptor -> header = MAJOR_TEXT
                hasFlag(depth - 1, RAW_BYTES) || hasFlag(depth, RAW_BYTES) -> {
                    if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                        error("${CborRawBytes::class.simpleName} annotation applied on an invalid field. It should be a List<Byte> or equivalent")
                    header = MAJOR_BYTE
                }

                else -> header = MAJOR_ARRAY
            }

            is StructureKind.MAP -> header = MAJOR_MAP
            else -> throw IllegalStateException("Try to encode a ${descriptor.kind} but SerialDescriptor isn't a StructureKind")
        }

        setFlag(depth, INFINITE)
        writer.write(header or SIZE_INFINITE)
        return this
    }


    override fun endStructure(descriptor: SerialDescriptor) {
        val infinite = hasFlag(depth, TAG_INFINITE or INFINITE)
        if (hasFlag(depth - 1, RAW_BYTES)) flush(infinite)
        if (infinite) writer.write(HEADER_BREAK)
        clear(depth)
        depth--
    }

    // region Composite

    // The default implementation of AbstractEncoder does not provide enough freedom to encode things correctly.
    // Compositing is omitted because the tracker array exists. With a single instance of CborEncoder, everything is much faster.

    open fun shouldEncodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (index < descriptor.elementsCount)
            for (annotation in descriptor.getElementAnnotations(index))
                if (annotation is CborSkip)
                    return false
        return true
    }

    private inline fun encodeElement(descriptor: SerialDescriptor, index: Int, block: () -> Unit) {
        if (hasFlag(depth, STRUCTURE)) encodeRawString(descriptor.getElementName(index).encodeToByteArray())
        ensureCapacity(1)
        depth++
        updateTrackerWithAnnotations(descriptor.getElementAnnotations(index))
        block()
        clear(depth)
        depth--
    }

    private inline fun <T> encodeElementAndReturn(descriptor: SerialDescriptor, index: Int, block: () -> T): T {
        if (hasFlag(depth, STRUCTURE)) encodeRawString(descriptor.getElementName(index).encodeToByteArray())
        depth++
        updateTrackerWithAnnotations(descriptor.getElementAnnotations(index))
        val result = block()
        clear(depth)
        depth--
        return result
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
        if (shouldEncodeElement(descriptor, index))
            encodeElement(descriptor, index) { serializer.serialize(this, value) }
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?
    ) {
        if (shouldEncodeElement(descriptor, index))
            encodeElement(descriptor, index) { encodeNullableSerializableValue(serializer, value) }
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        if (shouldEncodeElement(descriptor, index))
            encodeElementAndReturn(descriptor, index) { encodeInline(descriptor.getElementDescriptor(index)) }
        else CborEncoderNoOp


    // region PRIMITIVE ENCODING

    /**
     * An inlined class will have the class and the value annotated.
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

    private inline fun flush(infinite: Boolean) {
        if (!infinite || buffer.size == 0) return
        writer.writeMajor32(MAJOR_BYTE, buffer.size)
        writer.write(buffer.getSizedArray(), 0, buffer.size)
        buffer.size = 0
    }

    override fun encodeByte(value: Byte) {
        // @CborRawByte val data: ByteArray
        if (hasFlag(depth - 2, RAW_BYTES)) {
            // @CborInfinite @CborRawByte val data: ByteArray
            // Cbor.encodeToByteArray(CborByteArraySerializer, data)
            if (hasFlag(depth - 2, INFINITE) || hasFlag(depth - 1, INFINITE)) {
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

    open fun encodeUByte(value: UByte) {
        writer.writeMajor8(MAJOR_POSITIVE, value.toByte())
    }

    open fun encodeUByteNeg(value: UByte) {
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

    open fun encodeUShort(value: UShort) {
        writer.writeMajor16(MAJOR_POSITIVE, value.toShort())
    }

    open fun encodeUShortNeg(value: UShort) {
        writer.writeMajor16(MAJOR_NEGATIVE, value.toShort())
    }

    override fun encodeInt(value: Int) {
        if (value < 0) writer.writeMajor32(MAJOR_NEGATIVE, value xor INT_FF)
        else writer.writeMajor32(MAJOR_POSITIVE, value)
    }

    open fun encodeUInt(value: UInt) {
        writer.writeMajor32(MAJOR_POSITIVE, value.toInt())
    }

    open fun encodeUIntNeg(value: UInt) {
        writer.writeMajor32(MAJOR_NEGATIVE, value.toInt())
    }

    override fun encodeLong(value: Long) {
        if (value < 0) writer.writeMajor64(MAJOR_NEGATIVE, value xor LONG_FF)
        else writer.writeMajor64(MAJOR_POSITIVE, value)
    }

    open fun encodeULong(value: ULong) {
        writer.writeMajor64(MAJOR_POSITIVE, value.toLong())
    }

    open fun encodeULongNeg(value: ULong) {
        writer.writeMajor64(MAJOR_NEGATIVE, value.toLong())
    }

    override fun encodeFloat(value: Float) {
        val float16Bits = float32ToFloat16bits(value)
        // NaN != NaN. toRawBits is necessary
        if (float16BitsToFloat32(float16Bits).toRawBits() == value.toRawBits())
            writer.writeHeader16(HEADER_FLOAT_16, float16Bits.toShort())
        else writer.writeHeader32(HEADER_FLOAT_32, value.toRawBits())
    }

    override fun encodeDouble(value: Double) {
        val floatValue = float64toFloat32(value)
        // NaN != NaN. toRawBits is necessary
        if (floatValue.toDouble().toRawBits() == value.toRawBits()) encodeFloat(floatValue)
        else writer.writeHeader64(HEADER_FLOAT_64, value.toRawBits())
    }

    override fun encodeNull() = writer.write(HEADER_NULL)

    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeString(value: String) {
        if (hasFlag(depth - 1, INFINITE) || hasFlag(depth, INFINITE)) encodeStringInfinite(value)
        else encodeRawString(value.encodeToByteArray())
    }

    private fun encodeStringInfinite(value: String) {
        val descriptor = ListStringsDescriptor(name(CborEncoder::class))
        encodeStructure(descriptor) {
            for (item in value.chunkedSequence(255)) {
                // Force raw encoding instead of using encodeStringElement.
                // If not, it can result in infinite string encoded in infinite string
                encodeRawString(item.encodeToByteArray())
            }
        }
    }

    open fun encodeRawString(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_TEXT, count)
        if (count != 0) writer.write(value, offset, count)
    }

    open fun encodeBytesElement(serialDescriptor: SerialDescriptor, index: Int, value: ByteArray) = encodeBytes(value)
    open fun encodeBytes(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_BYTE, count)
        if (count != 0) writer.write(value, offset, count)
    }

    // TODO : Enums as ints ?
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    // endregion
}