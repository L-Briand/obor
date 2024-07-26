package net.orandja.obor

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.serializer
import net.orandja.obor.Resource.MajorNegative
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.decoder.CborDecoderException
import kotlin.test.Test
import kotlin.test.assertFailsWith


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
class MajorNegativeTest {

    companion object {
        const val EMPTY: Byte = -1
        const val SIZE_0: Byte = -0x18
        const val SIZE_8: Short = 0x80.xor(-1).toShort()
        const val SIZE_16: Int = 0x8000.xor(-1)
        const val SIZE_32: Long = 0x80000000L.xor(-1L)
        const val SIZE_64: Long = 0x1000000000000000.xor(-1L)
    }

    @Test
    fun empty() {
        assertTransformation(MajorNegative.EMPTY, Byte.serializer(), EMPTY)
        assertTransformation(MajorNegative.EMPTY, Short.serializer(), EMPTY.toShort())
        assertTransformation(MajorNegative.EMPTY, Int.serializer(), EMPTY.toInt())
        assertTransformation(MajorNegative.EMPTY, Long.serializer(), EMPTY.toLong())
    }

    @Test
    fun size0() {
        assertTransformation(MajorNegative.SIZE_0, Byte.serializer(), SIZE_0)
        assertTransformation(MajorNegative.SIZE_0, Short.serializer(), SIZE_0.toShort())
        assertTransformation(MajorNegative.SIZE_0, Int.serializer(), SIZE_0.toInt())
        assertTransformation(MajorNegative.SIZE_0, Long.serializer(), SIZE_0.toLong())
    }

    @Test
    fun size8() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorNegative.SIZE_8)
        }
        assertTransformation(MajorNegative.SIZE_8, Short.serializer(), SIZE_8)
        assertTransformation(MajorNegative.SIZE_8, Int.serializer(), SIZE_8.toInt())
        assertTransformation(MajorNegative.SIZE_8, Long.serializer(), SIZE_8.toLong())
    }

    @Test
    fun size16() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorNegative.SIZE_16)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorNegative.SIZE_16)
        }
        assertTransformation(MajorNegative.SIZE_16, Int.serializer(), SIZE_16)
        assertTransformation(MajorNegative.SIZE_16, Long.serializer(), SIZE_16.toLong())
    }

    @Test
    fun size32() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorNegative.SIZE_32)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorNegative.SIZE_32)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), MajorNegative.SIZE_32)
        }
        assertTransformation(MajorNegative.SIZE_32, Long.serializer(), SIZE_32)
    }

    @Test
    fun size64() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorNegative.SIZE_64)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorNegative.SIZE_64)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), MajorNegative.SIZE_64)
        }
        assertTransformation(MajorNegative.SIZE_64, Long.serializer(), SIZE_64)
    }

    @Test
    fun limit() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorNegative.LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorNegative.LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), MajorNegative.LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Long.serializer(), MajorNegative.LIMIT)
        }
    }
}