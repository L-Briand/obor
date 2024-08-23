package net.orandja.obor.codec

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborSkip
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.io.CborReader
import net.orandja.obor.io.ExpandableArray
import net.orandja.obor.io.ExpandableByteArray
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
internal class CborDecoder(
    private val reader: CborReader,
    override val serializersModule: SerializersModule,
) : Decoder, CompositeDecoder {

    // region Tracker

    // Metadata is stored in four int values [A, B, C, D] where:
    //   - A contains flags
    //   - B contains the major
    //   - C contains collectionSize
    //   - D contains the current index element

    companion object {
        // @formatter:off
        private const val INLINE    = 0b01
        private const val STRUCTURE = 0b10
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
     *
     * The tracker is a [ExpandableArray] in essence,
     * but it is more efficient to have it embedded in the class directly.
     */
    private var tracker = IntArray(16)

    private var depth = 0

    private inline fun ensureCapacity(elementsToAppend: Int) {
        if (depth * 4 + 1 + elementsToAppend * 4 <= tracker.size) return
        val newArray = IntArray((tracker.size + 1 + elementsToAppend * 4).takeHighestOneBit() shl 1)
        tracker.copyInto(newArray, 0, 0, tracker.size)
        tracker = newArray
    }

    private inline fun clear(depth: Int) {
        tracker[depth * 4] = 0
        tracker[depth * 4 + 1] = 0
        tracker[depth * 4 + 2] = 0
        tracker[depth * 4 + 3] = 0
    }

    private inline fun hasFlag(depth: Int, flag: Int): Boolean = tracker[depth * 4] and flag > 0
    private inline fun setFlag(depth: Int, flag: Int) {
        tracker[depth * 4] = tracker[depth * 4] or flag
    }

    private inline fun getMajor(depth: Int) = tracker[depth * 4 + 1].toByte()
    private inline fun setMajor(depth: Int, major: Byte) {
        tracker[depth * 4 + 1] = major.toInt() and 0xFF
    }

    private inline fun getCollectionSize(depth: Int) = (tracker[depth * 4 + 2])
    private inline fun setCollectionSize(depth: Int, size: Int) {
        tracker[depth * 4 + 2] = size
    }

    private inline fun getIndex(depth: Int) = tracker[depth * 4 + 3]
    private inline fun setIndex(depth: Int, index: Int) {
        tracker[depth * 4 + 3] = index
    }

    // endregion

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (!hasFlag(depth, INLINE)) {
            readTag(descriptor.annotations)
            ensureCapacity(1)
            depth++
        }

        val major = when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT -> {
                setFlag(depth, STRUCTURE)
                MAJOR_MAP
            }

            StructureKind.LIST -> {
                when {
                    descriptor is ByteArrayDescriptor || descriptor is ListBytesDescriptor -> MAJOR_BYTE
                    descriptor is ListStringsDescriptor -> MAJOR_TEXT
                    descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE
                            && reader.peek() hasMajor MAJOR_BYTE -> MAJOR_BYTE

                    else -> MAJOR_ARRAY
                }
            }

            StructureKind.MAP -> MAJOR_MAP

            else -> throw CborDecoderException.Default()
        }

        if (!(reader.peek() hasMajor major)) throw CborDecoderException.Default()
        setMajor(depth, major)
        setCollectionSize(depth, decodeCollectionSize(descriptor))
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (getCollectionSize(depth) == -1 && reader.peekConsume() != HEADER_BREAK) throw CborDecoderException.Default()
        clear(depth)
        depth--
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        readTag(descriptor.annotations)
        ensureCapacity(1)
        depth++
        setFlag(depth, INLINE)
        readTag(descriptor.getElementAnnotations(0))
        return this
    }

    // region TAG

    private fun readTag(annotations: List<Annotation>) {
        var tag: Long? = null
        var required = false
        for (annotation in annotations) if (annotation is CborTag) {
            tag = annotation.tag
            required = annotation.required
        }

        val noTag = !(reader.peek() hasMajor MAJOR_TAG)
        if (noTag && required) throw CborDecoderException.Default()
        if (noTag) return
        val sizeBits = reader.peekConsume() and SIZE_MASK
        val readTag: Long = when {
            sizeBits < SIZE_8 -> sizeBits.toLong()
            sizeBits == SIZE_8 -> reader.nextByte().toLong() and 0xFF
            sizeBits == SIZE_16 -> reader.nextShort().toLong() and 0xFFFF
            sizeBits == SIZE_32 -> reader.nextInt().toLong() and 0xFFFFFFFF
            sizeBits == SIZE_64 -> reader.nextLong()
            else -> throw CborDecoderException.Default()
        }
        if (tag != null && readTag != tag) throw CborDecoderException.Default()
        return
    }

    // endregion

    // region COMPOSITE

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (isCollectionDone()) return CompositeDecoder.DECODE_DONE
        if (hasFlag(depth, STRUCTURE)) return decodeStructureElementIndex(descriptor)
        val index = getIndex(depth)
        setIndex(depth, index + 1)
        return index
    }

    private inline fun isCollectionDone(): Boolean {
        val collectionSize = getCollectionSize(depth)
        val infinite = collectionSize == -1
        return (!infinite && getIndex(depth) == collectionSize) || (infinite && reader.peek() == HEADER_BREAK)
    }

    private fun decodeStructureElementIndex(descriptor: SerialDescriptor): Int {
        // - Decoded element not inside the kotlin object representation
        // - Element is explicitly skipped
        while (true) {
            val index = descriptor.getElementIndex(decodeString())
            val annotations = descriptor.getElementAnnotations(index)

            val skipped = annotations.any { it is CborSkip }
            if (!skipped && index != CompositeDecoder.UNKNOWN_NAME) { // element is not unknown
                setIndex(depth, getIndex(depth) + 1)
                return index
            }

            if (skipped) setIndex(depth, getIndex(depth) + 1) // only skipped field should increment internal index
            // else TODO: ignoreUnknownKeys

            skipElement()
            if (isCollectionDone()) return CompositeDecoder.DECODE_DONE
        }
    }

    /**
     * To avoid any missuses, It denies any size over [Int.MAX_VALUE]
     * Otherwise it implies that something will be created in memory with more than [Int.MAX_VALUE] bytes.
     * The language does not allow something like: ByteArray(Long > Int.MAX_VALUE).
     *
     * @return -1 when size is infinite
     */
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        val sizeBits = reader.peekConsume() and SIZE_MASK
        val result = when {
            sizeBits < SIZE_8 -> sizeBits.toInt()
            sizeBits == SIZE_8 -> reader.nextByte().toInt() and 0xFF
            sizeBits == SIZE_16 -> reader.nextShort().toInt() and 0xFFFF
            sizeBits == SIZE_32 -> {
                val result = reader.nextInt()
                if (result < 0) throw CborDecoderException.Default()
                result
            }

            sizeBits == SIZE_INFINITE -> return -1
            else -> throw CborDecoderException.Default()
        }

        // Map<*,*> requires double of the result size
        return if (getMajor(depth) == MAJOR_MAP && !hasFlag(depth, STRUCTURE)) result * 2 else result
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeBoolean()
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeByte()
    }

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeChar()
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeShort()
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeInt()
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeLong()
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeFloat()
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeDouble()
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeString()
    }

    fun decodeBytesElement(descriptor: SerialDescriptor, index: Int): ByteArray {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeBytes()
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = decodeInline(descriptor)

    private fun <T : Any?> decodeSerializableValue(
        deserializer: DeserializationStrategy<T>, previousValue: T? = null
    ): T = decodeSerializableValue(deserializer)

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?
    ): T {
        if (index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
        return decodeSerializableValue(deserializer, previousValue)
    }

    private inline fun <T : Any> Decoder.decodeIfNullable(
        deserializer: DeserializationStrategy<T?>, block: () -> T?
    ): T? {
        val isNullabilitySupported = deserializer.descriptor.isNullable
        return if (isNullabilitySupported || decodeNotNullMark()) block() else decodeNull()
    }

    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?
    ): T? = decodeIfNullable(deserializer) {
        decodeSerializableValue(deserializer, previousValue)
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
            HEADER_FLOAT_16 -> float16BitsToFloat32(reader.nextShort().toInt() and 0xFFFF)
            HEADER_FLOAT_32 -> Float.fromBits(reader.nextInt())
            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeDouble(): Double {
        return when (reader.peekConsume()) {
            HEADER_FLOAT_16 -> float16BitsToFloat32(reader.nextShort().toInt() and 0xFFFF).toDouble()
            HEADER_FLOAT_32 -> Float.fromBits(reader.nextInt()).toDouble()
            HEADER_FLOAT_64 -> Double.fromBits(reader.nextLong())
            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeByte(): Byte {
        // Discard intermittent size bytes in infinite bytes string
        if (getMajor(depth) == MAJOR_BYTE && getCollectionSize(depth) == -1) {
            val peek = reader.peek()
            if (peek and MAJOR_MASK == MAJOR_BYTE) skipSimpleElement(peek)
        }

        // Read next byte
        val header = reader.peekConsume()
        val major = header and MAJOR_MASK
        if (major != MAJOR_POSITIVE && major != MAJOR_NEGATIVE) throw CborDecoderException.Default()
        val size = header and SIZE_MASK
        return when {
            size < SIZE_8 -> if (major == MAJOR_NEGATIVE) size.xor(-1) else size
            size == SIZE_8 -> {
                val result = reader.nextByte()
                if (result < 0) throw CborDecoderException.Default()
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeShort(): Short {
        val header = reader.peekConsume()
        val major = header and MAJOR_MASK
        if (major != MAJOR_POSITIVE && major != MAJOR_NEGATIVE) throw CborDecoderException.Default()
        val size = header and SIZE_MASK
        return when {
            size < SIZE_8 -> {
                if (major == MAJOR_NEGATIVE) size.xor(-1).toShort() else size.toShort()
            }

            size == SIZE_8 -> {
                val result = reader.nextByte().toShort() and 0xFF
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            size == SIZE_16 -> {
                val result = reader.nextShort()
                if (result < 0) throw CborDecoderException.Default()
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeInt(): Int {
        val header = reader.peekConsume()
        val major = header and MAJOR_MASK
        if (major != MAJOR_POSITIVE && major != MAJOR_NEGATIVE) throw CborDecoderException.Default()
        val size = header and SIZE_MASK
        return when {
            size < SIZE_8 -> {
                if (major == MAJOR_NEGATIVE) size.xor(-1).toInt() else size.toInt()
            }

            size == SIZE_8 -> {
                val result = reader.nextByte().toInt() and 0xFF
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            size == SIZE_16 -> {
                val result = reader.nextShort().toInt() and 0xFFFF
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            size == SIZE_32 -> {
                val result = reader.nextInt()
                if (result < 0) throw CborDecoderException.Default()
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeLong(): Long {
        val header = reader.peekConsume()
        val major = header and MAJOR_MASK
        if (major != MAJOR_POSITIVE && major != MAJOR_NEGATIVE) throw CborDecoderException.Default()
        val size = header and SIZE_MASK
        return when {
            size < SIZE_8 -> {
                if (major == MAJOR_NEGATIVE) size.xor(-1).toLong() else size.toLong()
            }

            size == SIZE_8 -> {
                val result = reader.nextByte().toLong() and 0xFF
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            size == SIZE_16 -> {
                val result = reader.nextShort().toLong() and 0xFFFF
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            size == SIZE_32 -> {
                val result = reader.nextInt().toLong() and 0xFFFFFFFF
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            size == SIZE_64 -> {
                val result = reader.nextLong()
                if (result < 0) throw CborDecoderException.Default()
                if (major == MAJOR_NEGATIVE) result.xor(-1) else result
            }

            else -> throw CborDecoderException.Default()
        }
    }

    override fun decodeChar(): Char {
        val result = decodeString()
        return if (result.length == 1) result[0] else throw CborDecoderException.Default()
    }

    override fun decodeString(): String {
        val header = reader.peek()
        if (header and MAJOR_MASK != MAJOR_TEXT) throw CborDecoderException.Default()
        val sizeBits = header and SIZE_MASK
        val size: Int = when {
            sizeBits < SIZE_8 -> sizeBits.toInt()
            sizeBits == SIZE_8 -> reader.nextByte().toInt() and 0xFF
            sizeBits == SIZE_16 -> reader.nextShort().toInt() and 0xFFFF
            sizeBits == SIZE_32 -> {
                val result = reader.nextInt()
                if (result < 0) throw CborDecoderException.Default()
                result
            }

            sizeBits == SIZE_INFINITE -> return decodeStringInfinite()
            else -> throw CborDecoderException.Default()
        }
        reader.consume()
        return reader.readAsString(size)
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

    /**
     * This function exists to decode bytearrays (and infinite bytes string) more efficiently like a string.
     */
    fun decodeBytes(): ByteArray {
        val header = reader.peek()
        if (header and MAJOR_MASK != MAJOR_BYTE) throw CborDecoderException.Default()
        val sizeBits = header and SIZE_MASK
        val size: Int = when {
            sizeBits < SIZE_8 -> sizeBits.toInt()
            sizeBits == SIZE_8 -> reader.nextByte().toInt() and 0xFF
            sizeBits == SIZE_16 -> reader.nextShort().toInt() and 0xFFFF
            sizeBits == SIZE_32 -> {
                val result = reader.nextInt()
                if (result < 0) throw CborDecoderException.Default()
                result
            }

            sizeBits == SIZE_INFINITE -> return decodeBytesInfinite()
            else -> throw CborDecoderException.Default()
        }
        reader.consume()
        return reader.read(size)
    }

    private fun decodeBytesInfinite(): ByteArray {
        val result = ExpandableByteArray()
        var buffer: ByteArray
        val descriptor = ListBytesDescriptor(name(CborDecoder::class))
        decodeStructure(descriptor) {
            assertCborDecoder(::unreachable)
            var index: Int
            while (true) {
                index = decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                buffer = decodeBytesElement(descriptor.getElementDescriptor(index), index)
                result.write(buffer, 0, buffer.size);
            }
        }
        return result.getSizedArray()
    }

    // TODO: Add enum decoding by index.
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(decodeString())
    }

    // POSITIVE UNSIGNED

    fun decodeUByte(): UByte {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toUByte()
            HEADER_POSITIVE_8 -> reader.nextByte().toUByte()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUShort(): UShort {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toUShort()
            HEADER_POSITIVE_8 -> reader.nextByte().toUByte().toUShort()
            HEADER_POSITIVE_16 -> reader.nextShort().toUShort()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUInt(): UInt {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toUInt()
            HEADER_POSITIVE_8 -> reader.nextByte().toUByte().toUInt()
            HEADER_POSITIVE_16 -> reader.nextShort().toUShort().toUInt()
            HEADER_POSITIVE_32 -> reader.nextInt().toUInt()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeULong(): ULong {
        return when (val it = reader.peekConsume()) {
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and SIZE_MASK).toULong()
            HEADER_POSITIVE_8 -> reader.nextByte().toUByte().toULong()
            HEADER_POSITIVE_16 -> reader.nextShort().toUShort().toULong()
            HEADER_POSITIVE_32 -> reader.nextInt().toUInt().toULong()
            HEADER_POSITIVE_64 -> reader.nextLong().toULong()

            else -> throw CborDecoderException.Default()
        }
    }

    // NEGATIVE UNSIGNED

    fun decodeUByteNeg(): UByte {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toUByte()
            HEADER_NEGATIVE_8 -> reader.nextByte().toUByte().toByte().toUByte()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUShortNeg(): UShort {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toUShort()
            HEADER_NEGATIVE_8 -> reader.nextByte().toUByte().toUShort()
            HEADER_NEGATIVE_16 -> reader.nextShort().toUShort()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeUIntNeg(): UInt {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toUInt()
            HEADER_NEGATIVE_8 -> reader.nextByte().toUByte().toUInt()
            HEADER_NEGATIVE_16 -> reader.nextShort().toUShort().toUInt()
            HEADER_NEGATIVE_32 -> reader.nextInt().toUInt()
            else -> throw CborDecoderException.Default()
        }
    }

    fun decodeULongNeg(): ULong {
        return when (val it = reader.peekConsume()) {
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and SIZE_MASK).toULong()
            HEADER_NEGATIVE_8 -> reader.nextByte().toUByte().toULong()
            HEADER_NEGATIVE_16 -> reader.nextShort().toUShort().toULong()
            HEADER_NEGATIVE_32 -> reader.nextInt().toUInt().toULong()
            HEADER_NEGATIVE_64 -> reader.nextLong().toULong()

            else -> throw CborDecoderException.Default()
        }
    }

    // endregion

    //  region SKIP

    private fun skipElement() {
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
            (peek and SIZE_8) == SIZE_8 -> reader.nextByte().toUByte().toULong()
            (peek and SIZE_16) == SIZE_16 -> reader.nextShort().toUShort().toULong()
            (peek and SIZE_32) == SIZE_32 -> reader.nextInt().toUInt().toULong()
            (peek and SIZE_64) == SIZE_64 -> reader.nextLong().toULong()
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
}