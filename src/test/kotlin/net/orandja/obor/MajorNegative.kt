package net.orandja.obor

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.serializer
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.decoder.CborDecoderException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
@DisplayName("Major Negative")
class MajorNegative {

    companion object {
        const val EMPTY: Byte = -1
        const val SIZE_0: Byte = -0x18
        const val SIZE_8: Short = 0x80.xor(-1).toShort()
        const val SIZE_16: Int = 0x8000.xor(-1)
        const val SIZE_32: Long = 0x80000000L.xor(-1L)
        const val SIZE_64: Long = 0x1000000000000000.xor(-1L)

        const val RES_FILE = "major_negative"
        const val RES_ERROR = "Please check resources file $RES_FILE.cborhex"
    }

    private val tests = ResourceLoader.loadCborHex(RES_FILE)

    @Test
    @DisplayName("Empty")
    fun empty() {
        val empty = tests["EMPTY"] ?: error(RES_ERROR)
        assertTransformation(empty, Byte.serializer(), EMPTY)
        assertTransformation(empty, Short.serializer(), EMPTY.toShort())
        assertTransformation(empty, Int.serializer(), EMPTY.toInt())
        assertTransformation(empty, Long.serializer(), EMPTY.toLong())
    }

    @Test
    @DisplayName("Size 0")
    fun size0() {
        val size0 = tests["SIZE_0"] ?: error(RES_ERROR)
        assertTransformation(size0, Byte.serializer(), SIZE_0)
        assertTransformation(size0, Short.serializer(), SIZE_0.toShort())
        assertTransformation(size0, Int.serializer(), SIZE_0.toInt())
        assertTransformation(size0, Long.serializer(), SIZE_0.toLong())
    }

    @Test
    @DisplayName("Size 8")
    fun size8() {
        val size8 = tests["SIZE_8"] ?: error(RES_ERROR)
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Byte.serializer(), size8)
        }
        assertTransformation(size8, Short.serializer(), SIZE_8)
        assertTransformation(size8, Int.serializer(), SIZE_8.toInt())
        assertTransformation(size8, Long.serializer(), SIZE_8.toLong())
    }

    @Test
    @DisplayName("Size 16")
    fun size16() {
        val size16 = tests["SIZE_16"] ?: error(RES_ERROR)
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Byte.serializer(), size16)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Short.serializer(), size16)
        }
        assertTransformation(size16, Int.serializer(), SIZE_16)
        assertTransformation(size16, Long.serializer(), SIZE_16.toLong())
    }

    @Test
    @DisplayName("Size 32")
    fun size32() {
        val size32 = tests["SIZE_32"] ?: error(RES_ERROR)
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Byte.serializer(), size32)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Short.serializer(), size32)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Int.serializer(), size32)
        }
        assertTransformation(size32, Long.serializer(), SIZE_32)
    }

    @Test
    @DisplayName("Size 64")
    fun size64() {
        val size64 = tests["SIZE_64"] ?: error(RES_ERROR)
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Byte.serializer(), size64)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Short.serializer(), size64)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Int.serializer(), size64)
        }
        assertTransformation(size64, Long.serializer(), SIZE_64)
    }

    @Test
    @DisplayName("Limit")
    fun limit() {
        val limit = tests["LIMIT"] ?: error(RES_ERROR)
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Byte.serializer(), limit)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Short.serializer(), limit)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Int.serializer(), limit)
        }
        Assertions.assertThrows(CborDecoderException.Default::class.java) {
            Cbor.decodeFromByteArray(Long.serializer(), limit)
        }
    }
}