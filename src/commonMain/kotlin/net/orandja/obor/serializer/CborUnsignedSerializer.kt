package net.orandja.obor.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.orandja.obor.codec.assertCborDecoder
import net.orandja.obor.codec.assertCborEncoder
import net.orandja.obor.codec.name

sealed class CborUnsignedSerializer {

    // POSITIVE

    object UByte : CborUnsignedSerializer(), KSerializer<kotlin.UByte> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UByte::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UByte {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UByte::class) }
            return decoder.decodeUByte()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UByte) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UByte::class) }
            return encoder.encodeUByte(value)
        }
    }

    object UShort : CborUnsignedSerializer(), KSerializer<kotlin.UShort> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UShort::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UShort {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UShort::class) }
            return decoder.decodeUShort()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UShort) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UShort::class) }
            return encoder.encodeUShort(value)
        }
    }

    object UInt : CborUnsignedSerializer(), KSerializer<kotlin.UInt> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UInt::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UInt {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UInt::class) }
            return decoder.decodeUInt()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UInt) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UInt::class) }
            return encoder.encodeUInt(value)
        }
    }

    object ULong : CborUnsignedSerializer(), KSerializer<kotlin.ULong> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, ULong::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.ULong {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, ULong::class) }
            return decoder.decodeULong()
        }

        override fun serialize(encoder: Encoder, value: kotlin.ULong) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, ULong::class) }
            return encoder.encodeULong(value)
        }
    }

    // NEGATIVE

    object UByteNegative : CborUnsignedSerializer(), KSerializer<kotlin.UByte> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UByteNegative::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UByte {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UByteNegative::class) }
            return decoder.decodeNegativeUByte()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UByte) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UByteNegative::class) }
            return encoder.encodeNegativeUByte(value)
        }
    }

    object UShortNegative : CborUnsignedSerializer(), KSerializer<kotlin.UShort> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UShortNegative::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UShort {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UShortNegative::class) }
            return decoder.decodeNegativeUShort()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UShort) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UShortNegative::class) }
            return encoder.encodeNegativeUShort(value)
        }
    }

    object UIntNegative : CborUnsignedSerializer(), KSerializer<kotlin.UInt> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UIntNegative::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UInt {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UIntNegative::class) }
            return decoder.decodeNegativeUInt()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UInt) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UIntNegative::class) }
            return encoder.encodeNegativeUInt(value)
        }
    }

    object ULongNegative : CborUnsignedSerializer(), KSerializer<kotlin.ULong> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, ULongNegative::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.ULong {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, ULongNegative::class) }
            return decoder.decodeNegativeULong()
        }

        override fun serialize(encoder: Encoder, value: kotlin.ULong) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, ULongNegative::class) }
            return encoder.encodeNegativeULong(value)
        }
    }
}