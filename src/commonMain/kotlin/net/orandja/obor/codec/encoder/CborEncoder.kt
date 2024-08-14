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
import kotlin.experimental.xor

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
            writer.writeMajor64(MAJOR_TAG, tracker.encParentTag)
            tracker.encParentHasTag = false
        }
        if (tracker.encFieldHasTag) {
            writer.writeMajor64(MAJOR_TAG, tracker.encFieldTag)
            tracker.encFieldHasTag = false
        }
        if (tracker.encClassHasTag) {
            writer.writeMajor64(MAJOR_TAG, tracker.encClassTag)
            tracker.encClassHasTag = false
        }
    }

    protected fun setClassTracker(annotations: List<Annotation>) {
        for (annotation in annotations) {
            when (annotation) {
                is CborTag -> {
                    tracker.encClassHasTag = true
                    tracker.encClassTag = annotation.tag
                }

                is CborInfinite -> tracker.encClassIsInfinite = true
                is CborRawBytes -> tracker.encClassIsRawBytes = true
            }
        }
    }

    protected fun setFieldTracker(annotations: List<Annotation>) {
        if (tracker.encFieldSkipTag) return
        for (annotation in annotations) {
            when (annotation) {
                is CborTag -> {
                    tracker.encFieldHasTag = true
                    tracker.encFieldTag = annotation.tag
                }

                is CborInfinite -> tracker.encFieldIsInfinite = true
                is CborRawBytes -> tracker.encFieldIsRawBytes = true
            }
        }
    }

    open fun startCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        setClassTracker(descriptor.annotations)
        encodeTag()
        return this
    }

    open fun startStructure(descriptor: SerialDescriptor): CompositeEncoder {
        setClassTracker(descriptor.annotations)
        tracker.encClassIsInfinite = true
        encodeTag()
        return this
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (index < descriptor.elementsCount) {
            for (annotation in descriptor.getElementAnnotations(index)) if (annotation is CborSkip) return false
        }
        setFieldTracker(descriptor.getElementAnnotations(index))
        encodeTag()
        return super.encodeElement(descriptor, index)
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        setFieldTracker(descriptor.getElementAnnotations(0))
        tracker = newEncoderTracker(tracker)
        setClassTracker(descriptor.annotations)
        encodeTag()
        return super.encodeInline(descriptor)
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (tracker.encParentIsInfinite || tracker.encFieldIsInfinite || tracker.encClassIsInfinite)
            return beginStructure(descriptor)

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

    override fun encodeBoolean(value: Boolean) {
        if (value) writer.write(HEADER_TRUE)
        else writer.write(HEADER_FALSE)
    }

    override fun encodeByte(value: Byte) {
        if (value < 0) writer.writeMajor8(MAJOR_NEGATIVE, value xor BYTE_FF)
        else writer.writeMajor8(MAJOR_POSITIVE, value)
    }

    open fun encodeUByte(value: UByte) {
        writer.writeMajor8(MAJOR_POSITIVE, value.toByte())
    }

    open fun encodeUByteNeg(value: UByte) {
        writer.writeMajor8(MAJOR_NEGATIVE, value.toByte())
    }

    override fun encodeShort(value: Short) {
        if (value < 0) writer.writeMajor16(MAJOR_NEGATIVE, value xor SHORT_FF)
        else writer.writeMajor16(MAJOR_POSITIVE, value)
    }

    open fun encodeUShort(value: UShort) {
        writer.writeMajor16(MAJOR_POSITIVE, value.toShort())
    }

    open fun encodeUShortNeg(value: UShort) {
        writer.writeMajor16(MAJOR_NEGATIVE, value.toShort())
    }

    override fun encodeInt(value: Int) {
        if (value < 0) writer.writeMajor32(MAJOR_NEGATIVE, value xor INT_FF)
        else writer.writeMajor32(MAJOR_POSITIVE, value)
    }

    open fun encodeUInt(value: UInt) {
        writer.writeMajor32(MAJOR_POSITIVE, value.toInt())
    }

    open fun encodeUIntNeg(value: UInt) {
        writer.writeMajor32(MAJOR_NEGATIVE, value.toInt())
    }

    override fun encodeLong(value: Long) {
        if (value < 0) writer.writeMajor64(MAJOR_NEGATIVE, value xor LONG_FF)
        else writer.writeMajor64(MAJOR_POSITIVE, value)
    }

    open fun encodeULong(value: ULong) {
        writer.writeMajor64(MAJOR_POSITIVE, value.toLong())
    }

    open fun encodeULongNeg(value: ULong) {
        writer.writeMajor64(MAJOR_NEGATIVE, value.toLong())
    }

    override fun encodeFloat(value: Float) {
        val float16Bits = float32ToFloat16bits(value)
        // NaN != NaN. toRawBits is necessary
        if (float16BitsToFloat32(float16Bits).toRawBits() == value.toRawBits())
            writer.writeHeader16(HEADER_FLOAT_16, float16Bits.toShort())
        else writer.writeHeader32(HEADER_FLOAT_32, value.toRawBits())
    }

    override fun encodeDouble(value: Double) {
        val floatValue = float64toFloat32(value)
        // NaN != NaN. toRawBits is necessary
        if (floatValue.toDouble().toRawBits() == value.toRawBits()) encodeFloat(floatValue)
        else writer.writeHeader64(HEADER_FLOAT_64, value.toRawBits())
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
            for (item in value.chunkedSequence(255)) {
                // Force raw encoding instead of using encodeStringElement.
                // If not, it can result in infinite string encoded in infinite string
                encodeRawString(item.encodeToByteArray())
            }
        }
    }

    open fun encodeRawString(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_TEXT, count)
        if (count != 0) writer.write(value, offset, count)
    }


    open fun encodeBytesElement(serialDescriptor: SerialDescriptor, index: Int, value: ByteArray) = encodeBytes(value)
    open fun encodeBytes(value: ByteArray, offset: Int = 0, count: Int = value.size) {
        writer.writeMajor32(MAJOR_BYTE, count)
        if (count != 0) writer.write(value, offset, count)
    }

    // TODO : Enums as ints ?
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    // endregion

}