package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.*
import net.orandja.obor.codec.reader.CborReader
import kotlin.collections.set
import kotlin.experimental.xor

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal open class CborDecoder(
    protected val input: CborReader,
    override val serializersModule: SerializersModule
) : AbstractDecoder() {

    override fun decodeNotNullMark(): Boolean = input.peek() != HEADER_NULL
    override fun decodeNull(): Nothing? =
        if (input.peekConsume() == HEADER_NULL) null
        else throw CborDecoderException.Default

    override fun decodeBoolean(): Boolean = when (input.peekConsume()) {
        HEADER_FALSE -> false
        HEADER_TRUE -> true
        else -> throw CborDecoderException.Default
    }

    override fun decodeFloat(): Float = when (input.peekConsume()) {
        HEADER_FLOAT_32 -> Float.fromBits(input.nextUInt().toInt())
        else -> throw CborDecoderException.Default
    }

    override fun decodeDouble(): Double = when (input.peekConsume()) {
        HEADER_FLOAT_64 -> Double.fromBits(input.nextULong().toLong())
        else -> decodeFloat().toDouble()
    }

    override fun decodeByte(): Byte = when (val it = input.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toByte()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toByte().xor(-1)
        HEADER_POSITIVE_8 -> input.nextUByte().toByte()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_8 -> input.nextUByte().toByte().xor(-1)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeShort(): Short = when (val it = input.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toShort()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toShort().xor(-1)
        HEADER_POSITIVE_8 -> input.nextUByte().toShort()
        HEADER_NEGATIVE_8 -> input.nextUByte().toShort().xor(-1)
        HEADER_POSITIVE_16 -> input.nextUShort().toShort()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_16 -> input.nextUShort().toShort().xor(-1)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeInt(): Int = when (val it = input.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toInt()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toInt().xor(-1)
        HEADER_POSITIVE_8 -> input.nextUByte().toInt()
        HEADER_NEGATIVE_8 -> input.nextUByte().toInt().xor(-1)
        HEADER_POSITIVE_16 -> input.nextUShort().toInt()
        HEADER_NEGATIVE_16 -> input.nextUShort().toInt().xor(-1)
        HEADER_POSITIVE_32 -> input.nextUInt().toInt()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_32 -> input.nextUInt().toInt().xor(-1)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeLong(): Long = when (val it = input.peekConsume()) {
        in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toLong()
        in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toLong().xor(-1L)
        HEADER_POSITIVE_8 -> input.nextUByte().toLong()
        HEADER_NEGATIVE_8 -> input.nextUByte().toLong().xor(-1L)
        HEADER_POSITIVE_16 -> input.nextUShort().toLong()
        HEADER_NEGATIVE_16 -> input.nextUShort().toLong().xor(-1L)
        HEADER_POSITIVE_32 -> input.nextUInt().toLong()
        HEADER_NEGATIVE_32 -> input.nextUInt().toLong().xor(-1L)
        HEADER_POSITIVE_64 -> input.nextULong().toLong()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        HEADER_NEGATIVE_64 -> input.nextULong().toLong().xor(-1L)
            .takeIf { it < 0 } ?: throw CborDecoderException.Default
        else -> throw CborDecoderException.Default
    }

    override fun decodeChar(): Char = decodeString().singleOrNull() ?: throw CborDecoderException.Default
    override fun decodeString(): String {
        if (input.peek() == HEADER_TEXT_INFINITE) return decodeInfiniteString()
        val size: Int = when (val peek = input.peekConsume()) {
            in (HEADER_TEXT_START until HEADER_TEXT_8) -> (peek and 0x1Fu).toInt()
            HEADER_TEXT_8 -> input.nextUByte().toInt()
            HEADER_TEXT_16 -> input.nextUShort().toInt()
            HEADER_TEXT_32 -> input.nextUInt().toInt()
                .takeIf { it >= 0 } ?: throw CborDecoderException.Default
            HEADER_TEXT_64 -> throw CborDecoderException.Default
            else -> throw CborDecoderException.Default
        }
        return String(input.read(size).asByteArray(), Charsets.UTF_8)
    }

    private fun decodeInfiniteString(): String = decodeStructure(Descriptors.infiniteText) {
        val result = StringBuilder()
        var idx = -1
        while (decodeElementIndex(Descriptors.string) != CompositeDecoder.DECODE_DONE) {
            result.append(decodeStringElement(Descriptors.string, idx++))
        }
        result.toString()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(decodeString())

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = when (val it = input.peekConsume() and 0x1Fu) {
        in (SIZE_0 until SIZE_8) -> (it and 0x1Fu).toInt()
        SIZE_8 -> input.nextUByte().toInt()
        SIZE_16 -> input.nextUShort().toInt()
        SIZE_32 -> input.nextUInt().toInt()
            .takeIf { it >= 0 } ?: throw CborDecoderException.Default
        SIZE_64 -> throw CborDecoderException.Default
        SIZE_INFINITE -> throw CborDecoderException.Default // this one should have been check before calling decodeCollectionSize
        else -> throw CborDecoderException.Default
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (input.peek() hasFlags MAJOR_BYTE) return CborByteStringDecoder(input, serializersModule).beginStructure(descriptor)
        if (descriptor == Descriptors.infiniteText) return CborInfiniteTextDecoder(input, serializersModule).beginStructure(descriptor)
        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListDecoder(input, serializersModule).beginStructure(descriptor)
            is StructureKind.MAP -> CborMapDecoder(input, serializersModule).beginStructure(descriptor)
            else -> CborStructureDecoder(input, serializersModule).beginStructure(descriptor)
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = CompositeDecoder.DECODE_DONE

    override fun decodeValue(): Any {
        val it = input.peek()
        when {
            // TODO : Handle MAJOR_BYTE - crash when major byte is skip
            it hasFlags MAJOR_TEXT -> return decodeString()
            it hasFlags MAJOR_MAP -> return decodeMap()
            it hasFlags MAJOR_ARRAY -> return beginStructure(Descriptors.array)
        }
        input.consume()
        return when (it) {
            HEADER_FALSE -> false
            HEADER_TRUE -> true
            HEADER_FLOAT_32 -> Float.fromBits(input.nextUInt().toInt())
            HEADER_FLOAT_64 -> Double.fromBits(input.nextULong().toLong())
            in (HEADER_POSITIVE_START until HEADER_POSITIVE_8) -> (it and 0x1Fu).toByte()
            in (HEADER_NEGATIVE_START until HEADER_NEGATIVE_8) -> (it and 0x1Fu).toByte().xor(-1)
            HEADER_POSITIVE_8 -> with(input.nextUByte()) { toByte().takeIf { it >= 0 } ?: toShort() }
            HEADER_NEGATIVE_8 -> with(input.nextUByte()) { toByte().takeIf { it < 0 }?.xor(-1) ?: toShort().xor(-1) }
            HEADER_POSITIVE_16 -> with(input.nextUShort()) { toShort().takeIf { it >= 0 } ?: toInt() }
            HEADER_NEGATIVE_16 -> with(input.nextUShort()) { toShort().takeIf { it < 0 }?.xor(-1) ?: toInt().xor(-1) }
            HEADER_POSITIVE_32 -> with(input.nextUInt()) { toInt().takeIf { it >= 0 } ?: toLong() }
            HEADER_NEGATIVE_32 -> with(input.nextUInt()) { toInt().takeIf { it < 0 }?.xor(-1) ?: toLong().xor(-1L) }
            HEADER_POSITIVE_64 -> (input.nextULong().toLong().takeIf { it >= 0 } ?: throw CborDecoderException.Default)
            HEADER_NEGATIVE_64 -> ((input.nextULong()).toLong().takeIf { it < 0 }?.xor(1L) ?: throw CborDecoderException.Default)
            else -> super.decodeValue()
        }
    }

    private fun decodeArray() = decodeStructure(Descriptors.array) {
        (this as? CborDecoder) ?: throw CborDecoderException.Default
        val result = mutableListOf<Any>()
        while (decodeElementIndex(Descriptors.any) != CompositeDecoder.DECODE_DONE) {
            result += decodeValue()
        }
        result
    }

    private fun decodeMap() = decodeStructure(Descriptors.map) {
        (this as? CborDecoder) ?: throw CborDecoderException.Default
        val result = mutableMapOf<Any, Any>()
        while (true) {
            decodeElementIndex(Descriptors.any).takeIf { it != CompositeDecoder.DECODE_DONE } ?: break
            val key = decodeValue()
            decodeElementIndex(Descriptors.any).takeIf { it != CompositeDecoder.DECODE_DONE } ?: throw CborDecoderException.Default
            result[key] = decodeValue()
        }
        result
    }
}