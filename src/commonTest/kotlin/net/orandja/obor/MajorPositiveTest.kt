package net.orandja.obor

import kotlinx.serialization.builtins.serializer
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.decoder.CborDecoderException
import net.orandja.obor.serializer.CborUnsignedSerializer
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MajorPositiveTest {

    companion object {

        val CBOR_EMPTY = "00".hex()
        val CBOR_SIZE_0 = "17".hex()
        val CBOR_SIZE_8 = "1880".hex()
        val CBOR_SIZE_16 = "198000".hex()
        val CBOR_SIZE_32 = "1A80000000".hex()
        val CBOR_SIZE_64 = "1B1000000000000000".hex()
        val CBOR_LIMIT = "1B8000000000000000".hex()

        // @formatter:off
        const val EMPTY: Byte =   0
        const val SIZE_0: Byte =  0x17
        const val SIZE_8: Short = 0x80
        const val SIZE_16: Int =  0x8000
        const val SIZE_32: Long = 0x8000_0000
        const val SIZE_64: Long = 0x1000_0000_0000_0000

        const val UEMPTY: UByte =    0u
        const val USIZE_0: UByte =   0x17u
        const val USIZE_8: UByte =   0x80u
        const val USIZE_16: UShort = 0x8000u
        const val USIZE_32: UInt =   0x8000_0000u
        const val USIZE_64: ULong =  0x1000_0000_0000_0000u
        const val ULIMIT: ULong =    0x8000_0000_0000_0000u
        // @formatter:on
    }

    @Test
    fun empty() {
        assertTransformation(CBOR_EMPTY, EMPTY)
        assertTransformation(CBOR_EMPTY, EMPTY.toShort())
        assertTransformation(CBOR_EMPTY, EMPTY.toInt())
        assertTransformation(CBOR_EMPTY, EMPTY.toLong())

        assertTransformation(CBOR_EMPTY, UEMPTY, CborUnsignedSerializer.UByte)
        assertTransformation(CBOR_EMPTY, UEMPTY.toUShort(), CborUnsignedSerializer.UShort)
        assertTransformation(CBOR_EMPTY, UEMPTY.toUInt(), CborUnsignedSerializer.UInt)
        assertTransformation(CBOR_EMPTY, UEMPTY.toULong(), CborUnsignedSerializer.ULong)
    }

    @Test
    fun size0() {
        assertTransformation(CBOR_SIZE_0, SIZE_0)
        assertTransformation(CBOR_SIZE_0, SIZE_0.toShort())
        assertTransformation(CBOR_SIZE_0, SIZE_0.toInt())
        assertTransformation(CBOR_SIZE_0, SIZE_0.toLong())

        assertTransformation(CBOR_SIZE_0, USIZE_0, CborUnsignedSerializer.UByte)
        assertTransformation(CBOR_SIZE_0, USIZE_0.toUShort(), CborUnsignedSerializer.UShort)
        assertTransformation(CBOR_SIZE_0, USIZE_0.toUInt(), CborUnsignedSerializer.UInt)
        assertTransformation(CBOR_SIZE_0, USIZE_0.toULong(), CborUnsignedSerializer.ULong)
    }

    @Test
    fun size8() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_8)
        }
        assertTransformation(CBOR_SIZE_8, SIZE_8)
        assertTransformation(CBOR_SIZE_8, SIZE_8.toInt())
        assertTransformation(CBOR_SIZE_8, SIZE_8.toLong())

        assertTransformation(CBOR_SIZE_8, USIZE_8, CborUnsignedSerializer.UByte)
        assertTransformation(CBOR_SIZE_8, USIZE_8.toUShort(), CborUnsignedSerializer.UShort)
        assertTransformation(CBOR_SIZE_8, USIZE_8.toUInt(), CborUnsignedSerializer.UInt)
        assertTransformation(CBOR_SIZE_8, USIZE_8.toULong(), CborUnsignedSerializer.ULong)
    }

    @Test
    fun size16() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_16)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_SIZE_16)
        }
        assertTransformation(CBOR_SIZE_16, SIZE_16)
        assertTransformation(CBOR_SIZE_16, SIZE_16.toLong())

        assertTransformation(CBOR_SIZE_16, USIZE_16, CborUnsignedSerializer.UShort)
        assertTransformation(CBOR_SIZE_16, USIZE_16.toUInt(), CborUnsignedSerializer.UInt)
        assertTransformation(CBOR_SIZE_16, USIZE_16.toULong(), CborUnsignedSerializer.ULong)
    }

    @Test
    fun size32() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_32)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_SIZE_32)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), CBOR_SIZE_32)
        }
        assertTransformation(CBOR_SIZE_32, SIZE_32)

        assertTransformation(CBOR_SIZE_32, USIZE_32, CborUnsignedSerializer.UInt)
        assertTransformation(CBOR_SIZE_32, USIZE_32.toULong(), CborUnsignedSerializer.ULong)
    }

    @Test
    fun size64() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_SIZE_64)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_SIZE_64)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), CBOR_SIZE_64)
        }
        assertTransformation(CBOR_SIZE_64, SIZE_64)
        assertTransformation(CBOR_SIZE_64, USIZE_64, CborUnsignedSerializer.ULong)
    }

    @Test
    fun limit() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), CBOR_LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), CBOR_LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), CBOR_LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Long.serializer(), CBOR_LIMIT)
        }
        assertTransformation(CBOR_LIMIT, ULIMIT, CborUnsignedSerializer.ULong)
    }
}