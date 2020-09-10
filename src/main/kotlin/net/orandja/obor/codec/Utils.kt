@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.orandja.obor.codec

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.*

// Constants by encoders & decoders.
// used for better understanding in code.

internal const val MAJOR_POSITIVE: UByte = 0u
internal const val MAJOR_NEGATIVE: UByte = 32u
internal const val MAJOR_BYTE: UByte = 64u
internal const val MAJOR_TEXT: UByte = 96u
internal const val MAJOR_ARRAY: UByte = 128u
internal const val MAJOR_MAP: UByte = 160u
internal const val MAJOR_TAG: UByte = 192u
internal const val MAJOR_PRIMITIVE: UByte = 224u

internal const val SIZE_0: UByte = 0u
internal const val SIZE_8: UByte = 24u
internal const val SIZE_16: UByte = 25u
internal const val SIZE_32: UByte = 26u
internal const val SIZE_64: UByte = 27u
internal const val SIZE_INFINITE: UByte = 31u

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

internal val HEADER_FALSE: UByte = MAJOR_PRIMITIVE or 20u
internal val HEADER_TRUE: UByte = MAJOR_PRIMITIVE or 21u
internal val HEADER_NULL: UByte = MAJOR_PRIMITIVE or 22u
internal val HEADER_UNDEFINED: UByte = MAJOR_PRIMITIVE or 23u
internal val HEADER_BREAK: UByte = MAJOR_PRIMITIVE or 31u

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


@InternalSerializationApi
internal object Descriptors {

    // Used by decoder for Any deserialization
    val any = buildSerialDescriptor("net.orandja.obor.codec.Descriptors.any", SerialKind.CONTEXTUAL)
    val array = buildSerialDescriptor("net.orandja.obor.codec.Descriptors.array", StructureKind.LIST, any)
    val map = buildSerialDescriptor("net.orandja.obor.codec.Descriptors.map", StructureKind.MAP, any, any)

    // Used by codec for infinite text serialisation
    val string = PrimitiveSerialDescriptor("net.orandja.obor.codec.Descriptors.string", PrimitiveKind.STRING)
    val infiniteText = buildSerialDescriptor("net.orandja.obor.codec.Descriptors.any", StructureKind.LIST, string)
}

internal infix fun UByte.hasFlags(flags: UByte) = (this and flags) == flags