package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.*
import net.orandja.obor.io.ByteVector
import net.orandja.obor.io.CborReader
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * Default Cbor decoder.
 *
 * @param reader Something that reads Cbor header and bytes.
 * @see CborReader
 */
@OptIn(ExperimentalSerializationApi::class)
internal open class CborDecoder(
    protected val reader: CborReader,
    override val serializersModule: SerializersModule,
    protected var tracker: Array<Long> = newDecoderTracker()
) : AbstractDecoder() {


    open fun startStructure(descriptor: SerialDescriptor): CompositeDecoder {
        readTag(descriptor.annotations)
        decodeTags()
        return this
    }

    /**
     * To avoid any missuses: It denies any size over [Int.MAX_VALUE]
     * Otherwise it implies that something will be created in memory with more than [Int.MAX_VALUE] bytes.
     */
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int =
        when (val it = reader.peekConsume() and SIZE_MASK) {
            in (SIZE_0 until SIZE_8) -> (it and SIZE_MASK).toInt()
            SIZE_8 -> reader.nextUByte().toInt()
            SIZE_16 -> reader.nextUShort().toInt()
            SIZE_32 -> reader.nextUInt().toInt()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            SIZE_64 -> throw CborDecoderException.Default()
            SIZE_INFINITE -> throw CborDecoderException.Default() // this one should have been checked before calling decodeCollectionSize
            else -> throw CborDecoderException.Default()
        }

    /**
     * Check for structure type and delegate decoding to one of any implementation of [CborCollectionDecoder]
     * @see CborCollectionDecoder
     */
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        // infinite Major are in fact just arrays of say elements.
        when (descriptor) {
            is ByteArrayDescriptor,
            is ListBytesDescriptor ->
                return CborBytesDecoder(reader, serializersModule, tracker).startStructure(descriptor)

            is ListStringsDescriptor ->
                return CborTextDecoder(reader, serializersModule, tracker).startStructure(descriptor)
        }
        // List<Byte>, Array<Byte>, things like that decoding MAJOR_BYTE.
        if (descriptor.kind == StructureKind.LIST &&
            descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE &&
            reader.peek() hasMajor MAJOR_BYTE
        ) return CborByteStringDecoder(reader, serializersModule, tracker).startStructure(descriptor)

        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListDecoder(reader, serializersModule, tracker).startStructure(descriptor)
            is StructureKind.MAP -> CborMapDecoder(reader, serializersModule, tracker).startStructure(descriptor)
            else -> CborStructureDecoder(reader, serializersModule, tracker).startStructure(descriptor)
        }
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        readTag(descriptor.annotations)
        decodeTags()
        tracker = newDecoderTracker(tracker)
        readTag(descriptor.getElementAnnotations(0))
        decodeTags()
        return super.decodeInline(descriptor)
    }

    /** By default decoder do not have element. */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = CompositeDecoder.DECODE_DONE

    // region TAG

    protected fun readTag(annotations: List<Annotation>) {
        tracker.decClassHasTag = false
        for (annotation in annotations) {
            if (annotation is CborTag) {
                tracker.decClassHasTag = true
                tracker.decClassTag = annotation.tag
                tracker.decClassRequireTag = annotation.required
                return
            }
        }
    }

    open fun decodeTags() {
        if (tracker.decClassHasTag && decodeTag(tracker.decClassTag, tracker.decClassRequireTag)) {
            tracker.decClassHasTag = false
        }
        if (tracker.decParentHasTag && decodeTag(tracker.decParentTag, tracker.decParentRequireTag)) {
            tracker.decParentHasTag = false
        }
    }

    open fun decodeTag(tag: Long, isRequired: Boolean): Boolean {
        val noTag = !(reader.peek() hasMajor MAJOR_TAG)
        if (noTag && isRequired) throw CborDecoderException.Default()
        if (noTag) return false
        val readTag: Long = when (val peek = reader.peekConsume()) {
            in (HEADER_TAG_START until HEADER_TAG_8) -> (peek and 0x1F).toLong()
            HEADER_TAG_8 -> reader.nextUByte().toLong()
            HEADER_TAG_16 -> reader.nextUShort().toLong()
            HEADER_TAG_32 -> reader.nextUInt().toLong()
            HEADER_TAG_64 -> reader.nextULong().toLong()
            else -> throw CborDecoderException.Default()
        }
        if (readTag != tag) throw CborDecoderException.Default()
        return true
    }

    // endregion

    //  region SKIP

    protected fun skipElement() {
        val peek = reader.peek()
        when {
            peek hasMajor (MAJOR_BYTE or MAJOR_TEXT) -> when {
                peek and SIZE_INFINITE == SIZE_INFINITE -> skipRepeatInfinite()
                else -> reader.skip(getSize(peek).toInt())
            }

            peek hasMajor MAJOR_ARRAY -> when {
                peek and SIZE_INFINITE == SIZE_INFINITE -> skipRepeatInfinite()
                else -> skipRepeat(peek) { skipElement() }
            }

            peek hasMajor MAJOR_MAP -> when {
                peek and SIZE_INFINITE == SIZE_INFINITE -> skipRepeatInfinite()
                else -> skipRepeat(peek) { skipElement(); skipElement() }
            }

            else -> skipSimpleElement(peek)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getSize(peek: Byte): ULong {
        reader.consume()
        return when {
            (peek and SIZE_8) == SIZE_8 -> reader.nextUByte().toULong()
            (peek and SIZE_16) == SIZE_16 -> reader.nextUShort().toULong()
            (peek and SIZE_32) == SIZE_32 -> reader.nextUInt().toULong()
            (peek and SIZE_64) == SIZE_64 -> reader.nextULong()
            (peek and (MAJOR_MASK xor BYTE_FF) < SIZE_8) -> (peek and (MAJOR_MASK xor BYTE_FF)).toULong()
            else -> throw CborDecoderException.Default()
        }
    }

    private inline fun skipRepeat(peek: Byte, onItem: () -> Unit) {
        val count = getSize(peek)
        var index = 0.toULong()
        while (index < count) {
            onItem()
            index++
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun skipRepeatInfinite() {
        reader.consume()
        while (reader.peek() != HEADER_BREAK) skipElement()
        reader.consume()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun skipSimpleElement(peek: Byte) {
        reader.consume()
        when {
            (peek and SIZE_MASK) == SIZE_8 -> reader.skip(1)
            (peek and SIZE_MASK) == SIZE_16 -> reader.skip(2)
            (peek and SIZE_MASK) == SIZE_32 -> reader.skip(4)
            (peek and SIZE_MASK) == SIZE_64 -> reader.skip(8)
        }
    }

    // endregion

    // region PRIMITIVE DECODING

    override fun decodeNotNullMark(): Boolean {
        return reader.peek() != HEADER_NULL
    }

    override fun decodeNull(): Nothing? {
        return if (reader.peekConsume() == HEADER_NULL) null
        else throw CborDecoderException.Default()
    }

    override fun decodeBoolean(): Boolean {
        return when (reader.peekConsume()) {
            HEADER_FALSE -> false
            HEADER_TRUE -> true
            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeFloat(): Float {
        return when (reader.peekConsume()) {
            HEADER_FLOAT_16 -> float16BitsToFloat32(reader.nextUShort().toInt())
            HEADER_FLOAT_32 -> Float.fromBits(reader.nextUInt().toInt())
            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeDouble(): Double {
        return when (reader.peekConsume()) {
            HEADER_FLOAT_16 -> float16BitsToFloat32(reader.nextUShort().toInt()).toDouble()
            HEADER_FLOAT_32 -> Float.fromBits(reader.nextUInt().toInt()).toDouble()
            HEADER_FLOAT_64 -> Double.fromBits(reader.nextULong().toLong())
            else -> decodeFloat().toDouble()
        }
    }

    override fun decodeByte(): Byte {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK)
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).xor(-1)
            HEADER_POSITIVE_8 -> reader.nextUByte().toByte()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            HEADER_NEGATIVE_8 -> reader.nextUByte().toByte().xor(-1)
                .takeIf { it < 0 } ?: throw CborDecoderException.Default()

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeShort(): Short {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toShort()
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toShort().xor(-1)
            HEADER_POSITIVE_8 -> reader.nextUByte().toShort()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toShort().xor(-1)
            HEADER_POSITIVE_16 -> reader.nextUShort().toShort()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            HEADER_NEGATIVE_16 -> reader.nextUShort().toShort().xor(-1)
                .takeIf { it < 0 } ?: throw CborDecoderException.Default()

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeInt(): Int {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toInt()
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toInt().xor(-1)
            HEADER_POSITIVE_8 -> reader.nextUByte().toInt()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toInt().xor(-1)
            HEADER_POSITIVE_16 -> reader.nextUShort().toInt()
            HEADER_NEGATIVE_16 -> reader.nextUShort().toInt().xor(-1)
            HEADER_POSITIVE_32 -> reader.nextUInt().toInt()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            HEADER_NEGATIVE_32 -> reader.nextUInt().toInt().xor(-1)
                .takeIf { it < 0 } ?: throw CborDecoderException.Default()

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeLong(): Long {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toLong()
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toLong().xor(-1L)
            HEADER_POSITIVE_8 -> reader.nextUByte().toLong()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toLong().xor(-1L)
            HEADER_POSITIVE_16 -> reader.nextUShort().toLong()
            HEADER_NEGATIVE_16 -> reader.nextUShort().toLong().xor(-1L)
            HEADER_POSITIVE_32 -> reader.nextUInt().toLong()
            HEADER_NEGATIVE_32 -> reader.nextUInt().toLong().xor(-1L)
            HEADER_POSITIVE_64 -> reader.nextULong().toLong()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            HEADER_NEGATIVE_64 -> reader.nextULong().toLong().xor(-1L)
                .takeIf { it < 0 } ?: throw CborDecoderException.Default()

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeChar(): Char {
        val result = decodeString()
        return if (result.length == 1) result[0] else throw CborDecoderException.Default()
    }

    override fun decodeString(): String {

        if (reader.peek() == HEADER_TEXT_INFINITE) return decodeStringInfinite()

        val size: Int = when (val peek = reader.peekConsume()) {
            in (HEADER_TEXT_START until HEADER_TEXT_8) -> (peek and SIZE_MASK).toInt()
            HEADER_TEXT_8 -> reader.nextUByte().toInt()
            HEADER_TEXT_16 -> reader.nextUShort().toInt()
            HEADER_TEXT_32 -> reader.nextUInt().toInt()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            HEADER_TEXT_64 -> throw CborDecoderException.Default()
            else -> throw CborDecoderException.Default()
        }
        return reader.read(size).decodeToString()
    }

    private fun decodeStringInfinite(): String {
        val builder = StringBuilder()
        val descriptor = ListStringsDescriptor(name(CborDecoder::class))
        decodeStructure(descriptor) {
            assertCborDecoder(::unreachable)
            var index: Int
            while (true) {
                index = decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                builder.append(decodeStringElement(descriptor.getElementDescriptor(index), index))
            }
        }
        return builder.toString()
    }

    // Just to be consistent with the api
    fun decodeBytesElement(descriptor: SerialDescriptor, index: Int): ByteArray = decodeBytes()

    /**
     * This function exists to decode bytearrays (and infinite bytes string) more efficiently like a string.
     */
    fun decodeBytes(): ByteArray {

        if (reader.peek() == HEADER_BYTE_INFINITE) return decodeBytesInfinite()

        val size: Int = when (val peek = reader.peekConsume()) {
            in (HEADER_BYTE_START until HEADER_BYTE_8) -> (peek and SIZE_MASK).toInt()
            HEADER_BYTE_8 -> reader.nextUByte().toInt()
            HEADER_BYTE_16 -> reader.nextUShort().toInt()
            HEADER_BYTE_32 -> reader.nextUInt().toInt()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default()

            HEADER_BYTE_64 -> throw CborDecoderException.Default()
            else -> throw CborDecoderException.Default()
        }
        return reader.read(size)
    }

    private fun decodeBytesInfinite(): ByteArray {
        val result = ByteVector()
        var buffer: ByteArray
        val descriptor = ListBytesDescriptor(name(CborDecoder::class))
        decodeStructure(descriptor) {
            assertCborDecoder(::unreachable)
            var index: Int
            while (true) {
                index = decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                buffer = decodeBytesElement(descriptor.getElementDescriptor(index), index)
                result.add(buffer, 0, buffer.size);
            }
        }
        return result.nativeArray
    }

    // TODO: Add enum decoding by index.
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(decodeString())
    }

    // POSITIVE UNSIGNED

    // Quick note :
    // println(0xFFFF_FFFF) // 4294967295
    // println(0xFFFF_FFFF.toInt()) // -1
    // println(0xFFFF_FFFF.toInt().toLong()) // -1  ¯\_(ツ)_/¯

    fun decodeUByte(): UByte {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toUByte()
            HEADER_POSITIVE_8 -> reader.nextUByte()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUShort(): UShort {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toUShort()
            HEADER_POSITIVE_8 -> reader.nextUByte().toUShort()
            HEADER_POSITIVE_16 -> reader.nextUShort()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUInt(): UInt {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toUInt()
            HEADER_POSITIVE_8 -> reader.nextUByte().toUInt()
            HEADER_POSITIVE_16 -> reader.nextUShort().toUInt()
            HEADER_POSITIVE_32 -> reader.nextUInt()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeULong(): ULong {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toULong()
            HEADER_POSITIVE_8 -> reader.nextUByte().toULong()
            HEADER_POSITIVE_16 -> reader.nextUShort().toULong()
            HEADER_POSITIVE_32 -> reader.nextUInt().toULong()
            HEADER_POSITIVE_64 -> reader.nextULong()

            else -> throw CborDecoderException.Default()
        }
    }

    // NEGATIVE UNSIGNED

    fun decodeUByteNeg(): UByte {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toUByte()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toByte().toUByte()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUShortNeg(): UShort {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toUShort()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toUShort()
            HEADER_NEGATIVE_16 -> reader.nextUShort()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUIntNeg(): UInt {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toUInt()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toUInt()
            HEADER_NEGATIVE_16 -> reader.nextUShort().toUInt()
            HEADER_NEGATIVE_32 -> reader.nextUInt()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeULongNeg(): ULong {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toULong()
            HEADER_NEGATIVE_8 -> reader.nextUByte().toULong()
            HEADER_NEGATIVE_16 -> reader.nextUShort().toULong()
            HEADER_NEGATIVE_32 -> reader.nextUInt().toULong()
            HEADER_NEGATIVE_64 -> reader.nextULong()

            else -> throw CborDecoderException.Default()
        }
    }

    // endregion
}