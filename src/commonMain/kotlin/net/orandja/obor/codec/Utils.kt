@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.decoder.CborDecoder
import net.orandja.obor.codec.encoder.CborEncoder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

// Constants for encoders & decoders.
// Used to better understand the code.

internal const val MAJOR_POSITIVE: UByte = 0x00u
internal const val MAJOR_NEGATIVE: UByte = 0x20u
internal const val MAJOR_BYTE: UByte = 0x40u
internal const val MAJOR_TEXT: UByte = 0x60u
internal const val MAJOR_ARRAY: UByte = 0x80u
internal const val MAJOR_MAP: UByte = 0xA0u
internal const val MAJOR_TAG: UByte = 0xC0u
internal const val MAJOR_PRIMITIVE: UByte = 0xE0u
internal const val MAJOR_MASK: UByte = MAJOR_PRIMITIVE

internal const val SIZE_0: UByte = 0x00u
internal const val SIZE_8: UByte = 0x18u
internal const val SIZE_16: UByte = 0x19u
internal const val SIZE_32: UByte = 0x1Au
internal const val SIZE_64: UByte = 0x1Bu
internal const val SIZE_INFINITE: UByte = 0x1Fu

internal const val HEADER_POSITIVE_START: UByte = MAJOR_POSITIVE
internal val HEADER_POSITIVE_8: UByte = MAJOR_POSITIVE or SIZE_8
internal val HEADER_POSITIVE_16: UByte = MAJOR_POSITIVE or SIZE_16
internal val HEADER_POSITIVE_32: UByte = MAJOR_POSITIVE or SIZE_32
internal val HEADER_POSITIVE_64: UByte = MAJOR_POSITIVE or SIZE_64

internal const val HEADER_NEGATIVE_START: UByte = MAJOR_NEGATIVE
internal val HEADER_NEGATIVE_8: UByte = MAJOR_NEGATIVE or SIZE_8
internal val HEADER_NEGATIVE_16: UByte = MAJOR_NEGATIVE or SIZE_16
internal val HEADER_NEGATIVE_32: UByte = MAJOR_NEGATIVE or SIZE_32
internal val HEADER_NEGATIVE_64: UByte = MAJOR_NEGATIVE or SIZE_64

internal const val HEADER_BYTE_START: UByte = MAJOR_BYTE
internal val HEADER_BYTE_8: UByte = MAJOR_BYTE or SIZE_8
internal val HEADER_BYTE_16: UByte = MAJOR_BYTE or SIZE_16
internal val HEADER_BYTE_32: UByte = MAJOR_BYTE or SIZE_32
internal val HEADER_BYTE_64: UByte = MAJOR_BYTE or SIZE_64
internal val HEADER_BYTE_INFINITE: UByte = MAJOR_BYTE or SIZE_INFINITE

internal const val HEADER_TEXT_START: UByte = MAJOR_TEXT
internal val HEADER_TEXT_8: UByte = MAJOR_TEXT or SIZE_8
internal val HEADER_TEXT_16: UByte = MAJOR_TEXT or SIZE_16
internal val HEADER_TEXT_32: UByte = MAJOR_TEXT or SIZE_32
internal val HEADER_TEXT_64: UByte = MAJOR_TEXT or SIZE_64
internal val HEADER_TEXT_INFINITE: UByte = MAJOR_TEXT or SIZE_INFINITE

internal const val HEADER_ARRAY_START: UByte = MAJOR_ARRAY
internal val HEADER_ARRAY_8: UByte = MAJOR_ARRAY or SIZE_8
internal val HEADER_ARRAY_16: UByte = MAJOR_ARRAY or SIZE_16
internal val HEADER_ARRAY_32: UByte = MAJOR_ARRAY or SIZE_32
internal val HEADER_ARRAY_64: UByte = MAJOR_ARRAY or SIZE_64
internal val HEADER_ARRAY_INFINITE: UByte = MAJOR_ARRAY or SIZE_INFINITE

internal const val HEADER_MAP_START: UByte = MAJOR_MAP
internal val HEADER_MAP_8: UByte = MAJOR_MAP or SIZE_8
internal val HEADER_MAP_16: UByte = MAJOR_MAP or SIZE_16
internal val HEADER_MAP_32: UByte = MAJOR_MAP or SIZE_32
internal val HEADER_MAP_64: UByte = MAJOR_MAP or SIZE_64
internal val HEADER_MAP_INFINITE: UByte = MAJOR_MAP or SIZE_INFINITE

internal const val HEADER_TAG_START: UByte = MAJOR_TAG
internal val HEADER_TAG_8: UByte = MAJOR_TAG or SIZE_8
internal val HEADER_TAG_16: UByte = MAJOR_TAG or SIZE_16
internal val HEADER_TAG_32: UByte = MAJOR_TAG or SIZE_32
internal val HEADER_TAG_64: UByte = MAJOR_TAG or SIZE_64

internal val HEADER_FALSE: UByte = MAJOR_PRIMITIVE or 0x14u
internal val HEADER_TRUE: UByte = MAJOR_PRIMITIVE or 0x15u
internal val HEADER_NULL: UByte = MAJOR_PRIMITIVE or 0x16u
internal val HEADER_UNDEFINED: UByte = MAJOR_PRIMITIVE or 0x17u
internal val HEADER_BREAK: UByte = MAJOR_PRIMITIVE or 0x1Fu

internal val HEADER_FLOAT_16: UByte = MAJOR_PRIMITIVE or SIZE_16
internal val HEADER_FLOAT_32: UByte = MAJOR_PRIMITIVE or SIZE_32
internal val HEADER_FLOAT_64: UByte = MAJOR_PRIMITIVE or SIZE_64

internal val BYTE_NEG = (Byte.MIN_VALUE).toUByte()
internal val SHORT_NEG = (Short.MIN_VALUE).toUShort()
internal val INT_NEG = (Int.MIN_VALUE).toUInt()
internal val LONG_NEG = (Long.MIN_VALUE).toULong()
internal val BYTE_FF = (-1).toUByte()
internal val SHORT_FF = (-1).toUShort()
internal val INT_FF = (-1).toUInt()
internal val LONG_FF = (-1).toULong()

internal inline infix fun UByte.hasMajor(flags: UByte) = ((this and MAJOR_MASK) == flags)

internal inline fun <reified T> List<*>.findTypeOf() = firstOrNull { it is T } as? T

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> SerialDescriptor.findAnnotation() =
    annotations.firstOrNull { it is T } as? T

internal inline fun name(vararg klass: KClass<*>) = klass.map { it.simpleName }.joinToString(".")
internal inline fun unreachable(): Nothing = throw NotImplementedError("You shall not be here.")

@OptIn(ExperimentalContracts::class)
internal inline fun Decoder.assertCborDecoder(name: () -> String) {
    contract {
        callsInPlace(name, InvocationKind.AT_MOST_ONCE)
        returns() implies (this@assertCborDecoder is CborDecoder)
    }
    if (this !is CborDecoder) error("${name()} can only be used with Obor's library. Expected: ${CborDecoder::class} found ${this::class}")
}

@OptIn(ExperimentalContracts::class)
internal inline fun Encoder.assertCborEncoder(name: () -> String) {
    contract {
        callsInPlace(name, InvocationKind.AT_MOST_ONCE)
        returns() implies (this@assertCborEncoder is CborEncoder)
    }
    if (this !is CborEncoder) error("${name()} can only be used with Obor's library. Expected: ${CborEncoder::class} found ${this::class}")
}

@OptIn(ExperimentalContracts::class)
internal inline fun CompositeDecoder.assertCborDecoder(name: () -> String) {
    contract {
        callsInPlace(name, InvocationKind.AT_MOST_ONCE)
        returns() implies (this@assertCborDecoder is CborDecoder)
    }
    if (this !is CborDecoder) error("${name()} can only be used with Obor's library. Expected: ${CborDecoder::class} found ${this::class}")
}

@OptIn(ExperimentalContracts::class)
internal inline fun CompositeEncoder.assertCborEncoder(name: () -> String) {
    contract {
        callsInPlace(name, InvocationKind.AT_MOST_ONCE)
        returns() implies (this@assertCborEncoder is CborEncoder)
    }
    if (this !is CborEncoder) error("${name()} can only be used with Obor's library. Expected: ${CborEncoder::class} found ${this::class}")
}