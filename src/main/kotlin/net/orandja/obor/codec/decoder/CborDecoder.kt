package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.*
import net.orandja.obor.codec.reader.CborReader
import net.orandja.obor.vector.ByteVector
import kotlin.collections.set
import kotlin.experimental.xor

/**
 * Default Cbor decoder.
 *
 * @param reader Something that reads Cbor header and bytes.
 * @see CborReader
 */
@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal open class CborDecoder(
    protected val reader: CborReader,
    override val serializersModule: SerializersModule
) : AbstractDecoder() {

    override fun decodeNotNullMark(): Boolean = decodeTag() but reader.peek() != HEADER_NULL
    override fun decodeNull(): Nothing? = decodeTag() but
        if (reader.peekConsume() == HEADER_NULL) null
        else throw CborDecoderException.Default

    override fun decodeBoolean(): Boolean = decodeTag() but when (reader.peekConsume()) {
        HEADER_FALSE -> false
        HEADER_TRUE -> true
        else -> throw CborDecoderException.Default
    }

    // TODO : decode HEADER_FLOAT_16

    override fun decodeFloat(): Float = decodeTag() but when (reader.peekConsume()) {
        HEADER_FLOAT_32 -> Float.fromBits(reader.nextUInt().toInt())
        else -> throw CborDecoderException.Default
    }

    override fun decodeDouble(): Double = decodeTag() but when (reader.peekConsume()) {
        HEADER_FLOAT_64 -> Double.fromBits(reader.nextULong().toLong())
        else -> decodeFloat().toDouble()
    }

    override fun decodeByte(): Byte = decodeTag() but when (val it = reader.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toByte()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toByte().xor(-1)
        HEADER_POSITIVE_8 -> reader.nextUByte().toByte()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_8 -> reader.nextUByte().toByte().xor(-1)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeShort(): Short = decodeTag() but when (val it = reader.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toShort()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toShort().xor(-1)
        HEADER_POSITIVE_8 -> reader.nextUByte().toShort()
        HEADER_NEGATIVE_8 -> reader.nextUByte().toShort().xor(-1)
        HEADER_POSITIVE_16 -> reader.nextUShort().toShort()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_16 -> reader.nextUShort().toShort().xor(-1)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeInt(): Int = decodeTag() but when (val it = reader.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toInt()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toInt().xor(-1)
        HEADER_POSITIVE_8 -> reader.nextUByte().toInt()
        HEADER_NEGATIVE_8 -> reader.nextUByte().toInt().xor(-1)
        HEADER_POSITIVE_16 -> reader.nextUShort().toInt()
        HEADER_NEGATIVE_16 -> reader.nextUShort().toInt().xor(-1)
        HEADER_POSITIVE_32 -> reader.nextUInt().toInt()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_32 -> reader.nextUInt().toInt().xor(-1)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeLong(): Long = decodeTag() but when (val it = reader.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toLong()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toLong().xor(-1L)
        HEADER_POSITIVE_8 -> reader.nextUByte().toLong()
        HEADER_NEGATIVE_8 -> reader.nextUByte().toLong().xor(-1L)
        HEADER_POSITIVE_16 -> reader.nextUShort().toLong()
        HEADER_NEGATIVE_16 -> reader.nextUShort().toLong().xor(-1L)
        HEADER_POSITIVE_32 -> reader.nextUInt().toLong()
        HEADER_NEGATIVE_32 -> reader.nextUInt().toLong().xor(-1L)
        HEADER_POSITIVE_64 -> reader.nextULong().toLong()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_64 -> reader.nextULong().toLong().xor(-1L)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeChar(): Char = decodeTag() but decodeString().singleOrNull() ?: throw CborDecoderException.Default
    override fun decodeString(): String {
        decodeTag()
        if (reader.peek() == HEADER_TEXT_INFINITE) return decodeInfiniteString()
        val size: Int = when (val peek = reader.peekConsume()) {
            in (HEADER_TEXT_START until HEADER_TEXT_8) -> (peek and 0x1Fu).toInt()
            HEADER_TEXT_8 -> reader.nextUByte().toInt()
            HEADER_TEXT_16 -> reader.nextUShort().toInt()
            HEADER_TEXT_32 -> reader.nextUInt().toInt()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default
            HEADER_TEXT_64 -> throw CborDecoderException.Default
            else -> throw CborDecoderException.Default
        }
        return String(reader.read(size).asByteArray(), Charsets.UTF_8)
    }

    /** Custom defined function to decode Major 3 (String) infinite with serialization paradigm */
    private fun decodeInfiniteString(): String = decodeStructure(Descriptors.infiniteText) {
        val result = StringBuilder()
        var idx = -1
        while (decodeElementIndex(Descriptors.string) != CompositeDecoder.DECODE_DONE) {
            result.append(decodeStringElement(Descriptors.string, idx++))
        }
        result.toString()
    }

    // TODO: Add enum decoding by index.
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeTag() but enumDescriptor.getElementIndex(decodeString())

    /**
     * To avoid any missuses : It deny any size over [Int.MAX_VALUE]
     * because it implies that something will be created in memory with more than [Int.MAX_VALUE] bytes.
     */
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = when (val it = reader.peekConsume() and 0x1Fu) {
        in (SIZE_0 until SIZE_8) -> (it and 0x1Fu).toInt()
        SIZE_8 -> reader.nextUByte().toInt()
        SIZE_16 -> reader.nextUShort().toInt()
        SIZE_32 -> reader.nextUInt().toInt()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        SIZE_64 -> throw CborDecoderException.Default
        SIZE_INFINITE -> throw CborDecoderException.Default // this one should have been check before calling decodeCollectionSize
        else -> throw CborDecoderException.Default
    }

    /**
     * Check for structure type and delegate decoding to one of any implementation of [CborCollectionDecoder]
     * @see CborCollectionDecoder
     */
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (reader.peek() hasMajor MAJOR_BYTE) return CborByteStringDecoder(reader, serializersModule).beginStructure(descriptor)
        if (descriptor == Descriptors.infiniteText) return CborInfiniteTextDecoder(reader, serializersModule).beginStructure(descriptor)
        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListDecoder(reader, serializersModule).beginStructure(descriptor)
            is StructureKind.MAP -> CborMapDecoder(reader, serializersModule).beginStructure(descriptor)
            else -> CborStructureDecoder(reader, serializersModule).beginStructure(descriptor)
        }
    }

    /** By default decoder do not have element. */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = CompositeDecoder.DECODE_DONE

    override fun decodeValue(): Any {
        val it = reader.peek()
        when {
            it hasMajor MAJOR_BYTE -> return decodeByteString()
            it hasMajor MAJOR_TEXT -> return decodeString()
            it hasMajor MAJOR_MAP -> return decodeMap()
            it hasMajor MAJOR_ARRAY -> return decodeArray()
        }
        decodeTag()
        reader.consume()
        return when (it) {
            HEADER_FALSE -> false
            HEADER_TRUE -> true
            HEADER_FLOAT_32 -> Float.fromBits(reader.nextUInt().toInt())
            HEADER_FLOAT_64 -> Double.fromBits(reader.nextULong().toLong())
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toByte()
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toByte().xor(-1)
            HEADER_POSITIVE_8 -> with(reader.nextUByte()) { toByte().takeIf { it >= 0 } ?: toShort() }
            HEADER_NEGATIVE_8 -> with(reader.nextUByte()) { toByte().takeIf { it < 0 }?.xor(-1) ?: toShort().xor(-1) }
            HEADER_POSITIVE_16 -> with(reader.nextUShort()) { toShort().takeIf { it >= 0 } ?: toInt() }
            HEADER_NEGATIVE_16 -> with(reader.nextUShort()) { toShort().takeIf { it < 0 }?.xor(-1) ?: toInt().xor(-1) }
            HEADER_POSITIVE_32 -> with(reader.nextUInt()) { toInt().takeIf { it >= 0 } ?: toLong() }
            HEADER_NEGATIVE_32 -> with(reader.nextUInt()) { toInt().takeIf { it < 0 }?.xor(-1) ?: toLong().xor(-1L) }
            HEADER_POSITIVE_64 -> (reader.nextULong().toLong().takeIf { it >= 0 } ?: throw CborDecoderException.Default)
            HEADER_NEGATIVE_64 -> ((reader.nextULong()).toLong().takeIf { it < 0 }?.xor(1L) ?: throw CborDecoderException.Default)
            else -> super.decodeValue()
        }
    }

    /** Special decoder for byteString */
    private fun decodeByteString() = decodeStructure(Descriptors.array) {
        (this as? CborDecoder) ?: throw CborDecoderException.Default
        val result = ByteVector()
        while (decodeElementIndex(Descriptors.any) != CompositeDecoder.DECODE_DONE) {
            result.add(decodeByte())
        }
        result
    }

    /** Generic array decoding */
    private fun decodeArray() = decodeStructure(Descriptors.array) {
        (this as? CborDecoder) ?: throw CborDecoderException.Default
        val result = mutableListOf<Any>()
        while (decodeElementIndex(Descriptors.any) != CompositeDecoder.DECODE_DONE) {
            result += decodeValue()
        }
        result
    }

    /** Generic map decoding */
    private fun decodeMap() = decodeStructure(Descriptors.map) {
        (this as? CborDecoder) ?: throw CborDecoderException.Default
        val result = mutableMapOf<Any?, Any?>()
        while (true) {
            decodeElementIndex(Descriptors.any).takeIf { it != CompositeDecoder.DECODE_DONE } ?: break
            val key = if (!decodeNotNullMark()) decodeNull() else decodeValue()
            decodeElementIndex(Descriptors.any).takeIf { it != CompositeDecoder.DECODE_DONE } ?: throw CborDecoderException.Default
            result[key] = if (!decodeNotNullMark()) decodeNull() else decodeValue()
        }
        result
    }

    protected var requiredTag = -1L

    open fun decodeTag() {
        val require = requiredTag
        requiredTag = -1
        val noTag = !(reader.peek() hasMajor MAJOR_TAG)
        // field has a required tag but cbor message don't have one
        if (noTag && require != -1L) throw CborDecoderException.Default
        if (noTag) return
        val tag = when (val peek = reader.peekConsume()) {
            in (HEADER_TAG_START until HEADER_TAG_8) -> (peek and 0x1Fu).toLong()
            HEADER_TAG_8 -> reader.nextUByte().toLong()
            HEADER_TAG_16 -> reader.nextUShort().toLong()
            HEADER_TAG_32 -> reader.nextUInt().toLong()
            HEADER_TAG_64 -> reader.nextULong().toLong()
            else -> throw CborDecoderException.Default
        }
        if (require == -1L) return
        if (tag != require) throw CborDecoderException.Default
        requiredTag = -1L
    }

    private infix fun <T : Any?, R : Any?> T.but(r: R): R = r
}