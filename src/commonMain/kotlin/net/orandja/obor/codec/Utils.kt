@file:Suppress("NOTHING_TO_INLINE")

package net.orandja.obor.codec

import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.reflect.KClass

// Constants for encoders & decoders.
// Used to better understand the code.

internal const val MAJOR_POSITIVE: Byte = 0x00
internal const val MAJOR_NEGATIVE: Byte = 0x20
internal const val MAJOR_BYTE: Byte = 0x40
internal const val MAJOR_TEXT: Byte = 0x60
internal const val MAJOR_ARRAY: Byte = Byte.MIN_VALUE
internal const val MAJOR_MAP: Byte = -0x60
internal const val MAJOR_TAG: Byte = -0x40
internal const val MAJOR_PRIMITIVE: Byte = -0x20
internal const val MAJOR_MASK: Byte = MAJOR_PRIMITIVE

internal const val SIZE_8: Byte = 0x18
internal const val SIZE_16: Byte = 0x19
internal const val SIZE_32: Byte = 0x1A
internal const val SIZE_64: Byte = 0x1B
internal const val SIZE_INFINITE: Byte = 0x1F
internal const val SIZE_MASK: Byte = 0x1F

internal const val HEADER_POSITIVE_START: Byte = MAJOR_POSITIVE
internal val HEADER_POSITIVE_8: Byte = MAJOR_POSITIVE or SIZE_8
internal val HEADER_POSITIVE_16: Byte = MAJOR_POSITIVE or SIZE_16
internal val HEADER_POSITIVE_32: Byte = MAJOR_POSITIVE or SIZE_32
internal val HEADER_POSITIVE_64: Byte = MAJOR_POSITIVE or SIZE_64

internal const val HEADER_NEGATIVE_START: Byte = MAJOR_NEGATIVE
internal val HEADER_NEGATIVE_8: Byte = MAJOR_NEGATIVE or SIZE_8
internal val HEADER_NEGATIVE_16: Byte = MAJOR_NEGATIVE or SIZE_16
internal val HEADER_NEGATIVE_32: Byte = MAJOR_NEGATIVE or SIZE_32
internal val HEADER_NEGATIVE_64: Byte = MAJOR_NEGATIVE or SIZE_64

internal val HEADER_FALSE: Byte = MAJOR_PRIMITIVE or 0x14
internal val HEADER_TRUE: Byte = MAJOR_PRIMITIVE or 0x15
internal val HEADER_NULL: Byte = MAJOR_PRIMITIVE or 0x16
internal val HEADER_UNDEFINED: Byte = MAJOR_PRIMITIVE or 0x17
internal val HEADER_BREAK: Byte = MAJOR_PRIMITIVE or 0x1F

internal val HEADER_FLOAT_16: Byte = MAJOR_PRIMITIVE or SIZE_16
internal val HEADER_FLOAT_32: Byte = MAJOR_PRIMITIVE or SIZE_32
internal val HEADER_FLOAT_64: Byte = MAJOR_PRIMITIVE or SIZE_64

internal const val BYTE_FF: Byte = -1
internal const val SHORT_FF: Short = -1
internal const val INT_FF: Int = -1
internal const val LONG_FF: Long = -1

internal inline infix fun Byte.hasMajor(flags: Byte) = ((this and MAJOR_MASK) == flags)

// Others

internal inline fun name(vararg klass: KClass<*>) = klass.map { it.simpleName }.joinToString(".")
internal inline fun unreachable(): Nothing = throw NotImplementedError("Unreachable code reached.")

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