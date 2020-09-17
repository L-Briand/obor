package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.codec.*
import net.orandja.obor.codec.writer.CborWriter

/**
 * Default Cbor decoder.
 *
 * @param writer Something that writes Cbor header and bytes.
 * @see CborWriter
 */
@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal open class CborEncoder(
    protected val writer: CborWriter,
    override val serializersModule: SerializersModule
) : AbstractEncoder(), CompositeEncoder {

    override fun encodeBoolean(value: Boolean) =
        if (value) writer.write(HEADER_TRUE)
        else writer.write(HEADER_FALSE)

    override fun encodeByte(value: Byte) {
        val v = value.toUByte()
        if (v > BYTE_NEG) writer.writeMajor8(MAJOR_NEGATIVE, (v xor BYTE_FF))
        else writer.writeMajor8(MAJOR_POSITIVE, v)
    }

    override fun encodeShort(value: Short) {
        val v = value.toUShort()
        if (v > SHORT_NEG) writer.writeMajor16(MAJOR_NEGATIVE, (v xor SHORT_FF))
        else writer.writeMajor16(MAJOR_POSITIVE, v)
    }

    override fun encodeInt(value: Int) {
        val v = value.toUInt()
        if (v and INT_NEG > 0u) writer.writeMajor32(MAJOR_NEGATIVE, (v xor INT_FF))
        else writer.writeMajor32(MAJOR_POSITIVE, v)
    }

    override fun encodeLong(value: Long) {
        val v = value.toULong()
        if (v and LONG_NEG > 0u) writer.writeMajor64(MAJOR_NEGATIVE, (v xor LONG_FF))
        else writer.writeMajor64(MAJOR_POSITIVE, v)
    }

    override fun encodeFloat(value: Float) = writer.writeHeader32(HEADER_FLOAT_32, value.toRawBits().toUInt())
    override fun encodeDouble(value: Double) = writer.writeHeader64(HEADER_FLOAT_64, value.toRawBits().toULong())
    override fun encodeNull() = writer.write(HEADER_NULL)

    // chars are UTF-16 and can't be translated to UTF-8... or maybe ? didn't find.
    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writer.writeMajor32(MAJOR_TEXT, bytes.size.toUInt())
        writer.write(bytes.asUByteArray())
    }

    // TODO : Enums as ints
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    /** used by Collections encoders to indicate the size of a chunk before an element become infinite */
    protected open var chunkSize: Int = -1

    /** next element is a CBOR Byte string */
    protected open var isRawBytes: Boolean = false

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (descriptor == Descriptors.infiniteText)
            return CborInfiniteTextEncoder(writer, serializersModule, -1).beginCollection(descriptor, collectionSize)

        if (isRawBytes) {
            if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                throw IllegalStateException("${CborRawBytes::class} should be a list or array of bytes")
            return CborByteStringEncoder(writer, serializersModule, chunkSize).beginCollection(descriptor, collectionSize)
        }

        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListEncoder(writer, serializersModule, chunkSize).beginCollection(descriptor, collectionSize)
            is StructureKind.MAP -> CborMapEncoder(writer, serializersModule, chunkSize).beginCollection(descriptor, collectionSize)
            else -> beginStructure(descriptor)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor == Descriptors.infiniteText)
            return CborInfiniteTextEncoder(writer, serializersModule, -1).beginStructure(descriptor)

        if (isRawBytes) {
            if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                throw IllegalStateException("${CborRawBytes::class} should be a list or array of bytes")
            return CborByteStringEncoder(writer, serializersModule, chunkSize).beginStructure(descriptor)
        }

        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListEncoder(writer, serializersModule, -1).beginStructure(descriptor)
            is StructureKind.MAP -> CborMapEncoder(writer, serializersModule, -1).beginStructure(descriptor)
            is StructureKind.CLASS, is StructureKind.OBJECT ->
                CborStructureEncoder(writer, serializersModule, chunkSize).beginCollection(descriptor, descriptor.elementsCount)
            else -> throw IllegalStateException("Try to encode a ${descriptor.kind} but SerialDescriptor isn't a StructureKind")
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) = Unit

    override fun encodeValue(value: Any) = when (value) {
        is Boolean -> encodeBoolean(value)
        is Byte -> encodeByte(value)
        is Short -> encodeShort(value)
        is Int -> encodeInt(value)
        is Long -> encodeLong(value)
        is Float -> encodeFloat(value)
        is Double -> encodeDouble(value)
        is Char -> encodeChar(value)
        is String -> encodeString(value)

        is BooleanArray -> encodeStructure(Descriptors.array) { value.forEach { encodeBoolean(it) } }
        is ByteArray -> encodeStructure(Descriptors.array) { value.forEach { encodeByte(it) } }
        is ShortArray -> encodeStructure(Descriptors.array) { value.forEach { encodeShort(it) } }
        is IntArray -> encodeStructure(Descriptors.array) { value.forEach { encodeInt(it) } }
        is LongArray -> encodeStructure(Descriptors.array) { value.forEach { encodeLong(it) } }
        is FloatArray -> encodeStructure(Descriptors.array) { value.forEach { encodeFloat(it) } }
        is DoubleArray -> encodeStructure(Descriptors.array) { value.forEach { encodeDouble(it) } }
        is CharArray -> encodeStructure(Descriptors.array) { value.forEach { encodeChar(it) } }

        is Enum<*> -> encodeEnum(Descriptors.enum, value.ordinal)

        is Map<*, *> -> encodeStructure(Descriptors.array) {
            value.forEach {
                encodeNullableSerializableValue(serializersModule.serializer(), it.key)
                encodeNullableSerializableValue(serializersModule.serializer(), it.value)
            }
        }
        is Iterable<*> -> encodeStructure(Descriptors.array) {
            value.forEach { encodeNullableSerializableValue(serializersModule.serializer(), it) }
        }
        else -> super.encodeValue(value)
    }
}