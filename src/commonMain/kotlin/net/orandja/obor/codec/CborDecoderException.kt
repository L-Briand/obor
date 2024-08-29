package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.experimental.and


sealed class CborDecoderException(
    index: Long,
    message: String,
) : IllegalStateException("Failed to decode CBOR data at index $index. Reason: $message") {

    @OptIn(ExperimentalSerializationApi::class)
    class InvalidStructureKind(index: Long, descriptor: SerialDescriptor) : CborDecoderException(
        index, "Tried to decode structure with an invalid SerialKind: ${descriptor.kind} (descriptor: $descriptor)"
    )

    @OptIn(ExperimentalSerializationApi::class)
    class InvalidMajor(index: Long, expected: Byte, found: Byte, descriptor: SerialDescriptor) : CborDecoderException(
        index, "Expected major $expected for SerialKind ${descriptor.kind}, found $found (descriptor: $descriptor)"
    )

    class RequiredTagNotFound(index: Long, expected: Long) : CborDecoderException(
        index, "Required tag $expected not found."
    )

    class UnexpectedTag(index: Long, expected: Long, found: Long) : CborDecoderException(
        index, "Unexpected tag found. Expected: $expected, Found: $found."
    )

    class CollectionSizeTooLarge(index: Long, size: ULong) : CborDecoderException(
        index,
        "Size of collection is too large (Size: $size) Collection should not exceed 2^31 (Int.MAX_VALUE) elements."
    )

    class InvalidSizeElement(index: Long, size: Byte, max: Byte, infinite: Boolean) : CborDecoderException(
        index,
        "Header Bits (0x1F) of sized value (${size and SIZE_MASK}) are not in (0..$max)${if (infinite) " or is 0xFF" else ""}."
    )

    class FailedToDecodeElement(index: Long, message: String) : CborDecoderException(
        index, "Failed to decode element: $message"
    )

    class UnfitValue(index: Long, name: String, size: ULong) : CborDecoderException(
        index, "Tried to decode '$name' but read size was too big ($size)"
    )

    class InvalidUnsignedValue(index: Long, name: String, header: Byte) : CborDecoderException(
        index,
        "Tried to decode '$name' but read header (Major: ${header and MAJOR_MASK}, Size: ${header and SIZE_MASK})"
    )

    class ClassUnknownKey(index: Long, key: String, descriptor: SerialDescriptor) : CborDecoderException(
        index, "An unknown key was found while deserializing. (Name: $key, descriptor: $descriptor)"
    )
}