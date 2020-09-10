package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.*
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal open class CborEncoder(
    protected val out: CborWriter,
    override val serializersModule: SerializersModule
) : AbstractEncoder(), CompositeEncoder {

    override fun encodeBoolean(value: Boolean) =
        if (value) out.write(HEADER_TRUE)
        else out.write(HEADER_FALSE)

    override fun encodeByte(value: Byte) {
        val v = value.toUByte()
        if (v > BYTE_NEG) out.writeMajor8(MAJOR_NEGATIVE, (v xor BYTE_FF))
        else out.writeMajor8(MAJOR_POSITIVE, v)
    }

    override fun encodeShort(value: Short) {
        val v = value.toUShort()
        if (v > SHORT_NEG) out.writeMajor16(MAJOR_NEGATIVE, (v xor SHORT_FF))
        else out.writeMajor16(MAJOR_POSITIVE, v)
    }

    override fun encodeInt(value: Int) {
        val v = value.toUInt()
        if (v and INT_NEG > 0u) out.writeMajor32(MAJOR_NEGATIVE, (v xor INT_FF))
        else out.writeMajor32(MAJOR_POSITIVE, v)
    }

    override fun encodeLong(value: Long) {
        val v = value.toULong()
        if (v and LONG_NEG > 0u) out.writeMajor64(MAJOR_NEGATIVE, (v xor LONG_FF))
        else out.writeMajor64(MAJOR_POSITIVE, v)
    }

    override fun encodeFloat(value: Float) = out.writeHeader32(HEADER_FLOAT_32, value.toRawBits().toUInt())
    override fun encodeDouble(value: Double) = out.writeHeader64(HEADER_FLOAT_64, value.toRawBits().toULong())
    override fun encodeNull() = out.write(HEADER_NULL)

    // chars are UTF-16 and can't be translated to UTF-8... or maybe ? didn't find.
    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        out.writeMajor32(MAJOR_TEXT, bytes.size.toUInt())
        out.write(bytes.asUByteArray())
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    /** used by Collections encoders to indicate the size of a chunk before an element become infinite */
    protected open var chunkSize: Int = -1

    /** indicate that the next element is a CBOR Byte string */
    protected open var isRawBytes: Boolean = false

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (descriptor == Descriptors.infiniteText)
            return CborInfiniteTextEncoder(out, serializersModule, -1).beginCollection(descriptor, collectionSize)

        if (isRawBytes) {
            if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                throw IllegalStateException("@RawByte should be a list or array of bytes")
            return CborByteStringEncoder(out, serializersModule, chunkSize).beginCollection(descriptor, collectionSize)
        }

        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListEncoder(out, serializersModule, chunkSize).beginCollection(descriptor, collectionSize)
            is StructureKind.MAP -> CborMapEncoder(out, serializersModule, chunkSize).beginCollection(descriptor, collectionSize)
            else -> beginStructure(descriptor)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor == Descriptors.infiniteText)
            return CborInfiniteTextEncoder(out, serializersModule, -1).beginStructure(descriptor)

        if (isRawBytes) {
            if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                throw IllegalStateException("@RawByte should be a list or array of bytes")
            return CborByteStringEncoder(out, serializersModule, chunkSize).beginStructure(descriptor)
        }

        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListEncoder(out, serializersModule, -1).beginStructure(descriptor)
            is StructureKind.MAP -> CborMapEncoder(out, serializersModule, -1).beginStructure(descriptor)
            is StructureKind.CLASS, is StructureKind.OBJECT ->
                CborStructureEncoder(out, serializersModule, chunkSize).beginCollection(descriptor, descriptor.elementsCount)
            else -> throw IllegalStateException("Try to encode a structure but SerialDescriptor isn't a structure")
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) = Unit
}