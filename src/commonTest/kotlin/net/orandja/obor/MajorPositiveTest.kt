package net.orandja.obor

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.serializer
import net.orandja.obor.Resource.MajorPositive
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.decoder.CborDecoderException
import kotlin.test.Test
import kotlin.test.assertFailsWith


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
class MajorPositiveTest {

    companion object {
        const val EMPTY: Byte = 0
        const val SIZE_0: Byte = 0x17
        const val SIZE_8: Short = 0x80
        const val SIZE_16: Int = 0x8000
        const val SIZE_32: Long = 0x80000000
        const val SIZE_64: Long = 0x1000000000000000
    }

    @Test
    fun empty() {
        assertTransformation(MajorPositive.EMPTY, Byte.serializer(), EMPTY)
        assertTransformation(MajorPositive.EMPTY, Short.serializer(), EMPTY.toShort())
        assertTransformation(MajorPositive.EMPTY, Int.serializer(), EMPTY.toInt())
        assertTransformation(MajorPositive.EMPTY, Long.serializer(), EMPTY.toLong())
    }

    @Test
    fun size0() {
        assertTransformation(MajorPositive.SIZE_0, Byte.serializer(), SIZE_0)
        assertTransformation(MajorPositive.SIZE_0, Short.serializer(), SIZE_0.toShort())
        assertTransformation(MajorPositive.SIZE_0, Int.serializer(), SIZE_0.toInt())
        assertTransformation(MajorPositive.SIZE_0, Long.serializer(), SIZE_0.toLong())
    }

    @Test
    fun size8() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorPositive.SIZE_8)
        }
        assertTransformation(MajorPositive.SIZE_8, Short.serializer(), SIZE_8)
        assertTransformation(MajorPositive.SIZE_8, Int.serializer(), SIZE_8.toInt())
        assertTransformation(MajorPositive.SIZE_8, Long.serializer(), SIZE_8.toLong())
    }

    @Test
    fun size16() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorPositive.SIZE_16)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorPositive.SIZE_16)
        }
        assertTransformation(MajorPositive.SIZE_16, Int.serializer(), SIZE_16)
        assertTransformation(MajorPositive.SIZE_16, Long.serializer(), SIZE_16.toLong())
    }

    @Test
    fun size32() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorPositive.SIZE_32)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorPositive.SIZE_32)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), MajorPositive.SIZE_32)
        }
        assertTransformation(MajorPositive.SIZE_32, Long.serializer(), SIZE_32)
    }

    @Test
    fun size64() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorPositive.SIZE_64)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorPositive.SIZE_64)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), MajorPositive.SIZE_64)
        }
        assertTransformation(MajorPositive.SIZE_64, Long.serializer(), SIZE_64)
    }

    @Test
    fun limit() {
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Byte.serializer(), MajorPositive.LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Short.serializer(), MajorPositive.LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Int.serializer(), MajorPositive.LIMIT)
        }
        assertFailsWith(CborDecoderException.Default::class) {
            Cbor.decodeFromByteArray(Long.serializer(), MajorPositive.LIMIT)
        }
    }
}