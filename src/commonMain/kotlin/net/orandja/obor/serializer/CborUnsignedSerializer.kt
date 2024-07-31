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

    object UByteNeg : CborUnsignedSerializer(), KSerializer<kotlin.UByte> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UByteNeg::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UByte {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UByteNeg::class) }
            return decoder.decodeUByteNeg()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UByte) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UByteNeg::class) }
            return encoder.encodeUByteNeg(value)
        }
    }

    object UShortNeg : CborUnsignedSerializer(), KSerializer<kotlin.UShort> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UShortNeg::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UShort {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UShortNeg::class) }
            return decoder.decodeUShortNeg()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UShort) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UShortNeg::class) }
            return encoder.encodeUShortNeg(value)
        }
    }

    object UIntNeg : CborUnsignedSerializer(), KSerializer<kotlin.UInt> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, UIntNeg::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.UInt {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, UIntNeg::class) }
            return decoder.decodeUIntNeg()
        }

        override fun serialize(encoder: Encoder, value: kotlin.UInt) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, UIntNeg::class) }
            return encoder.encodeUIntNeg(value)
        }
    }

    object ULongNeg : CborUnsignedSerializer(), KSerializer<kotlin.ULong> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            name(CborUnsignedSerializer::class, ULongNeg::class),
            PrimitiveKind.BYTE
        )

        override fun deserialize(decoder: Decoder): kotlin.ULong {
            decoder.assertCborDecoder { name(CborUnsignedSerializer::class, ULongNeg::class) }
            return decoder.decodeULongNeg()
        }

        override fun serialize(encoder: Encoder, value: kotlin.ULong) {
            encoder.assertCborEncoder { name(CborUnsignedSerializer::class, ULongNeg::class) }
            return encoder.encodeULongNeg(value)
        }
    }
}