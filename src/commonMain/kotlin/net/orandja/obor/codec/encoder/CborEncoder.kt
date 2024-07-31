package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.annotations.CborSkip
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.*
import net.orandja.obor.io.CborWriter
import net.orandja.obor.serializer.CborListByteArraySerializer

/**
 * Default Cbor decoder.
 *
 * @param writer Something that writes Cbor header and bytes.
 * @see CborWriter
 */
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class)
internal open class CborEncoder(
    protected val writer: CborWriter,
    override val serializersModule: SerializersModule,
    protected open var tracker: Array<Long> = newEncoderTracker(),
) : AbstractEncoder() {

    protected fun encodeTag() {
        if (tracker.encParentHasTag) {
            writer.writeMajor64(MAJOR_TAG, tracker.encParentTag.toULong())
            tracker.encParentHasTag = false
        }
        if (tracker.encFieldHasTag) {
            writer.writeMajor64(MAJOR_TAG, tracker.encFieldTag.toULong())
            tracker.encFieldHasTag = false
        }
        if (tracker.encClassHasTag) {
            writer.writeMajor64(MAJOR_TAG, tracker.encClassTag.toULong())
            tracker.encClassHasTag = false
        }
    }

    open fun startCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (descriptor.annotations.findTypeOf<CborInfinite>() != null) tracker.encClassIsInfinite = true
        if (descriptor.annotations.findTypeOf<CborRawBytes>() != null) tracker.encClassIsRawBytes = true
        val cTag = descriptor.annotations.findTypeOf<CborTag>()
        if (cTag != null) {
            tracker.encClassHasTag = true
            tracker.encClassTag = cTag.tag
        }
        encodeTag()
        return this
    }

    open fun startStructure(descriptor: SerialDescriptor): CompositeEncoder {
        tracker.encClassIsInfinite = true
        if (descriptor.annotations.findTypeOf<CborRawBytes>() != null) tracker.encClassIsRawBytes = true
        val cTag = descriptor.annotations.findTypeOf<CborTag>()
        if (cTag != null) {
            tracker.encClassHasTag = true
            tracker.encClassTag = cTag.tag
        }
        encodeTag()
        return this
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (descriptor.getElementAnnotations(index).findTypeOf<CborSkip>() != null) return false
        encodeTag()
        return super.encodeElement(descriptor, index)
    }

    /** In cases of an inlined class, we need to get the only field annotation */
    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        val annotations = descriptor.getElementAnnotations(0)
        if (annotations.findTypeOf<CborInfinite>() != null) tracker.encFieldIsInfinite = true
        if (annotations.findTypeOf<CborRawBytes>() != null) tracker.encFieldIsRawBytes = true
        val cTag = annotations.findTypeOf<CborTag>()
        if (cTag != null) {
            tracker.encFieldHasTag = true
            tracker.encFieldTag = cTag.tag
        }
        tracker = newEncoderTracker(tracker)
        return super.encodeInline(descriptor)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (tracker.encParentIsInfinite || tracker.encFieldIsInfinite || tracker.encClassIsInfinite)
            return beginStructure(descriptor)

        encodeTag()
        when (descriptor) {
            is ByteArrayDescriptor,
            is ListBytesDescriptor ->
                return CborBytesEncoder(writer, serializersModule, tracker)
                    .startCollection(descriptor, collectionSize)

            is ListStringsDescriptor ->
                return CborTextEncoder(writer, serializersModule, tracker)
                    .startCollection(descriptor, collectionSize)
        }

        if (tracker.encFieldIsRawBytes) {
            if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                error("${CborRawBytes::class.simpleName} annotation applied on an invalid field. It should be a List<Byte> or equivalent")

            return CborByteStringEncoder(writer, serializersModule, tracker)
                .startCollection(descriptor, collectionSize)
        }

        return when (descriptor.kind) {
            is StructureKind.MAP ->
                CborMapEncoder(writer, serializersModule, tracker)
                    .startCollection(descriptor, collectionSize)

            is StructureKind.LIST ->
                CborListEncoder(writer, serializersModule, tracker)
                    .startCollection(descriptor, collectionSize)

            else -> beginStructure(descriptor)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        encodeTag()
        when (descriptor) {
            is ByteArrayDescriptor,
            is ListBytesDescriptor ->
                return CborBytesEncoder(writer, serializersModule, tracker).startStructure(descriptor)

            is ListStringsDescriptor ->
                return CborTextEncoder(writer, serializersModule, tracker).startStructure(descriptor)
        }

        if (tracker.encFieldIsRawBytes) {
            if (!(descriptor.kind is StructureKind.LIST && descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE))
                error("${CborRawBytes::class.simpleName} annotation applied on an invalid field. It should be a List<Byte> or equivalent")
            return CborByteStringEncoder(writer, serializersModule, tracker).startStructure(descriptor)
        }

        return when (descriptor.kind) {
            is StructureKind.LIST -> CborListEncoder(writer, serializersModule, tracker)
                .startStructure(descriptor)

            is StructureKind.MAP -> CborMapEncoder(writer, serializersModule, tracker)
                .startStructure(descriptor)

            is StructureKind.CLASS,
            is StructureKind.OBJECT -> CborStructureEncoder(writer, serializersModule, tracker)
                .startCollection(descriptor, descriptor.elementsCount)

            else -> throw IllegalStateException("Try to encode a ${descriptor.kind} but SerialDescriptor isn't a StructureKind")
        }
    }


    // region PRIMITIVE ENCODING

    override fun encodeBoolean(value: Boolean) = if (value) writer.write(HEADER_TRUE)
    else writer.write(HEADER_FALSE)

    override fun encodeByte(value: Byte) {
        val v = value.toUByte()
        if (v > BYTE_NEG) writer.writeMajor8(MAJOR_NEGATIVE, (v xor BYTE_FF))
        else writer.writeMajor8(MAJOR_POSITIVE, v)
    }

    open fun encodeUByte(value: UByte) = writer.writeMajor8(MAJOR_POSITIVE, value)
    open fun encodeUByteNeg(value: UByte) = writer.writeMajor8(MAJOR_NEGATIVE, value)

    override fun encodeShort(value: Short) {
        val v = value.toUShort()
        if (v > SHORT_NEG) writer.writeMajor16(MAJOR_NEGATIVE, (v xor SHORT_FF))
        else writer.writeMajor16(MAJOR_POSITIVE, v)
    }

    open fun encodeUShort(value: UShort) = writer.writeMajor16(MAJOR_POSITIVE, value)
    open fun encodeUShortNeg(value: UShort) = writer.writeMajor16(MAJOR_NEGATIVE, value)

    override fun encodeInt(value: Int) {
        val v = value.toUInt()
        if (v and INT_NEG > 0u) writer.writeMajor32(MAJOR_NEGATIVE, (v xor INT_FF))
        else writer.writeMajor32(MAJOR_POSITIVE, v)
    }

    open fun encodeUInt(value: UInt) = writer.writeMajor32(MAJOR_POSITIVE, value)
    open fun encodeUIntNeg(value: UInt) = writer.writeMajor32(MAJOR_NEGATIVE, value)

    override fun encodeLong(value: Long) {
        val v = value.toULong()
        if (v and LONG_NEG > 0u) writer.writeMajor64(MAJOR_NEGATIVE, (v xor LONG_FF))
        else writer.writeMajor64(MAJOR_POSITIVE, v)
    }

    open fun encodeULong(value: ULong) = writer.writeMajor64(MAJOR_POSITIVE, value)
    open fun encodeULongNeg(value: ULong) = writer.writeMajor64(MAJOR_NEGATIVE, value)

    override fun encodeFloat(value: Float) {
        val float16Bits = float32ToFloat16bits(value)
        // NaN != NaN. toRawBits is necessary
        if (float16BitsToFloat32(float16Bits).toRawBits() == value.toRawBits())
            writer.writeHeader16(HEADER_FLOAT_16, float16Bits.toUShort())
        else writer.writeHeader32(HEADER_FLOAT_32, value.toRawBits().toUInt())
    }

    override fun encodeDouble(value: Double) {
        // In JS, floating points are always double values.
        // Even when doing double.toFloat() (Function is in submodule folder)
        val floatValue = float64toFloat32(value)
        // NaN != NaN. toRawBits is necessary
        if (floatValue.toDouble().toRawBits() == value.toRawBits()) encodeFloat(floatValue)
        else writer.writeHeader64(HEADER_FLOAT_64, value.toRawBits().toULong())
    }

    override fun encodeNull() = writer.write(HEADER_NULL)

    protected fun encodeStructureName(value: String) = encodeRawString(value.encodeToByteArray())

    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeString(value: String) {
        if (tracker.encParentIsInfinite || tracker.encFieldIsInfinite) encodeStringInfinite(value)
        else encodeRawString(value.encodeToByteArray())
    }

    private fun encodeStringInfinite(value: String) {
        val descriptor = ListStringsDescriptor(name(CborEncoder::class))
        encodeStructure(descriptor) {
            for ((index, item) in value.chunkedSequence(255).withIndex()) {
                encodeStringElement(CborListByteArraySerializer.descriptor.getElementDescriptor(index), index, item)
            }
        }
    }

    open fun encodeRawString(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_TEXT, count.toUInt())
        if (count != 0) writer.write(value, offset, count)
    }


    open fun encodeBytesElement(serialDescriptor: SerialDescriptor, index: Int, value: ByteArray) = encodeBytes(value)
    open fun encodeBytes(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_BYTE, count.toUInt())
        if (count != 0) writer.write(value, offset, count)
    }

    // TODO : Enums as ints ?
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    // endregion

}