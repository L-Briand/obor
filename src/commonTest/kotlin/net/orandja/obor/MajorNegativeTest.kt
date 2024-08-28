package net.orandja.obor

import kotlinx.serialization.builtins.serializer
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.CborDecoderException
import net.orandja.obor.serializer.CborUnsignedSerializer
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MajorNegativeTest {

    companion object {
        val CBOR_EMPTY = "20".hex()
        val CBOR_SIZE_0 = "37".hex()
        val CBOR_SIZE_8 = "3880".hex()
        val CBOR_SIZE_16 = "398000".hex()
        val CBOR_SIZE_32 = "3A80000000".hex()
        val CBOR_SIZE_64 = "3B1000000000000000".hex()
        val CBOR_LIMIT = "3B8000000000000000".hex()

        const val EMPTY: Byte = -1
        const val SIZE_0: Byte = 0x17.xor(-1).toByte()
        const val SIZE_8: Short = 0x80.xor(-1).toShort()
        const val SIZE_16: Int = 0x8000.xor(-1)
        const val SIZE_32: Long = 0x80000000L.xor(-1L)
        const val SIZE_64: Long = 0x1000000000000000.xor(-1L)

        const val UEMPTY: UByte = 0u
        const val USIZE_0: UByte = 0x17u
        const val USIZE_8: UByte = 0x80u
        const val USIZE_16: UShort = 0x80_00u
        const val USIZE_32: UInt = 0x80_00_00_00u
        const val USIZE_64: ULong = 0x10_00_00_00_00_00_00_00u
        const val ULIMIT: ULong = 0x80_00_00_00_00_00_00_00u
    }

    @Test
    fun empty() {
        assertTransformation(CBOR_EMPTY, EMPTY)
        assertTransformation(CBOR_EMPTY, EMPTY.toShort())
        assertTransformation(CBOR_EMPTY, EMPTY.toInt())
        assertTransformation(CBOR_EMPTY, EMPTY.toLong())

        assertTransformation(CBOR_EMPTY, UEMPTY, CborUnsignedSerializer.UByteNeg)
        assertTransformation(CBOR_EMPTY, UEMPTY.toUShort(), CborUnsignedSerializer.UShortNeg)
        assertTransformation(CBOR_EMPTY, UEMPTY.toUInt(), CborUnsignedSerializer.UIntNeg)
        assertTransformation(CBOR_EMPTY, UEMPTY.toULong(), CborUnsignedSerializer.ULongNeg)
    }

    @Test
    fun size0() {
        assertTransformation(CBOR_SIZE_0, SIZE_0)
        assertTransformation(CBOR_SIZE_0, SIZE_0.toShort())
        assertTransformation(CBOR_SIZE_0, SIZE_0.toInt())
        assertTransformation(CBOR_SIZE_0, SIZE_0.toLong())

        assertTransformation(CBOR_SIZE_0, USIZE_0, CborUnsignedSerializer.UByteNeg)
        assertTransformation(CBOR_SIZE_0, USIZE_0.toUShort(), CborUnsignedSerializer.UShortNeg)
        assertTransformation(CBOR_SIZE_0, USIZE_0.toUInt(), CborUnsignedSerializer.UIntNeg)
        assertTransformation(CBOR_SIZE_0, USIZE_0.toULong(), CborUnsignedSerializer.ULongNeg)
    }

    @Test
    fun size8() {
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_8)
        }
        assertTransformation(CBOR_SIZE_8, SIZE_8)
        assertTransformation(CBOR_SIZE_8, SIZE_8.toInt())
        assertTransformation(CBOR_SIZE_8, SIZE_8.toLong())
        assertTransformation(CBOR_SIZE_8, USIZE_8, CborUnsignedSerializer.UByteNeg)
        assertTransformation(CBOR_SIZE_8, USIZE_8.toUShort(), CborUnsignedSerializer.UShortNeg)
        assertTransformation(CBOR_SIZE_8, USIZE_8.toUInt(), CborUnsignedSerializer.UIntNeg)
        assertTransformation(CBOR_SIZE_8, USIZE_8.toULong(), CborUnsignedSerializer.ULongNeg)
    }

    @Test
    fun size16() {
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_16)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_SIZE_16)
        }
        assertTransformation(CBOR_SIZE_16, SIZE_16)
        assertTransformation(CBOR_SIZE_16, SIZE_16.toLong())

        assertTransformation(CBOR_SIZE_16, USIZE_16, CborUnsignedSerializer.UShortNeg)
        assertTransformation(CBOR_SIZE_16, USIZE_16.toUInt(), CborUnsignedSerializer.UIntNeg)
        assertTransformation(CBOR_SIZE_16, USIZE_16.toULong(), CborUnsignedSerializer.ULongNeg)
    }

    @Test
    fun size32() {
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_32)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_SIZE_32)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Int.serializer(), CBOR_SIZE_32)
        }
        assertTransformation(CBOR_SIZE_32, SIZE_32)

        assertTransformation(CBOR_SIZE_32, USIZE_32, CborUnsignedSerializer.UIntNeg)
        assertTransformation(CBOR_SIZE_32, USIZE_32.toULong(), CborUnsignedSerializer.ULongNeg)
    }

    @Test
    fun size64() {
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_64)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_SIZE_64)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Int.serializer(), CBOR_SIZE_64)
        }
        assertTransformation(CBOR_SIZE_64, SIZE_64)
        assertTransformation(CBOR_SIZE_64, USIZE_64, CborUnsignedSerializer.ULongNeg)
    }

    @Test
    fun limit() {
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_LIMIT)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_LIMIT)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Int.serializer(), CBOR_LIMIT)
        }
        assertFailsWith(CborDecoderException::class) {
            Cbor.decodeFromByteArray(Long.serializer(), CBOR_LIMIT)
        }
        assertTransformation(CBOR_LIMIT, ULIMIT, CborUnsignedSerializer.ULongNeg)
    }
}