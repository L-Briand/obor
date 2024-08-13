package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.serializer.CborArrayByteArraySerializer
import net.orandja.obor.serializer.CborByteArraySerializer
import net.orandja.obor.serializer.CborListByteArraySerializer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MajorStringByteTest {

    companion object {

        // 0 ITEM
        val EMPTY: ByteArray = byteArrayOf()
        val CBOR_EMPTY = buildCbor { writeMajor8(MAJOR_BYTE, 0u) }
        val CBOR_EMPTY_INFINITE = buildChunkedInfinite(MAJOR_BYTE, 0, 10)

        // 1 ITEM
        val ONE: ByteArray = byteArrayOf(0)
        val CBOR_ONE = buildSize(MAJOR_BYTE, 1)
        val CBOR_ONE_INFINITE_x10 = buildChunkedInfinite(MAJOR_BYTE, 1, 10)
        val CBOR_ONE_INFINITE_x255 = buildChunkedInfinite(MAJOR_BYTE, 1, 255)

        // 23 ITEMS
        val MAX_SIZE0: ByteArray = ByteArray(0x17)
        val CBOR_MAX_SIZE0: ByteArray = buildSize(MAJOR_BYTE, 0x17)
        val CBOR_MAX_SIZE0_INFINITE_x10: ByteArray = buildChunkedInfinite(MAJOR_BYTE, 0x17, 10)
        val CBOR_MAX_SIZE0_INFINITE_x255: ByteArray = buildChunkedInfinite(MAJOR_BYTE, 0x17, 255)

        // 24 ITEMS
        val MIN_SIZE8: ByteArray = ByteArray(0x18)
        val CBOR_MIN_SIZE8 = buildSize(MAJOR_BYTE, 0x18)
        val CBOR_MIN_SIZE8_INFINITE_x10 = buildChunkedInfinite(MAJOR_BYTE, 0x18, 10)
        val CBOR_MIN_SIZE8_INFINITE_x255 = buildChunkedInfinite(MAJOR_BYTE, 0x18, 255)

        // 255 ITEMS
        val MAX_SIZE8: ByteArray = ByteArray(0xFF)
        val CBOR_MAX_SIZE8 = buildSize(MAJOR_BYTE, 0xFF)
        val CBOR_MAX_SIZE8_INFINITE_x10 = buildChunkedInfinite(MAJOR_BYTE, 0xFF, 10)
        val CBOR_MAX_SIZE8_INFINITE_x255 = buildChunkedInfinite(MAJOR_BYTE, 0xFF, 0xFF)

        // 257 ITEMS
        val MIN_SIZE16: ByteArray = ByteArray(0x100)
        val CBOR_MIN_SIZE16 = buildSize(MAJOR_BYTE, 0x100)
        val CBOR_MIN_SIZE16_INFINITE_x10 = buildChunkedInfinite(MAJOR_BYTE, 0x100, 10)
        val CBOR_MIN_SIZE16_INFINITE_x255 = buildChunkedInfinite(MAJOR_BYTE, 0x100, 0xFF)
    }

    @Serializable
    data class Bytes1(@CborRawBytes val bytes: ByteArray) {
        override fun hashCode(): Int = bytes.contentHashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes1) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }
    }

    @Serializable
    data class Bytes2(@CborInfinite @CborRawBytes val bytes: ByteArray) {
        override fun hashCode(): Int = bytes.contentHashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes2) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }
    }

    @Serializable
    data class BytesArray1(@CborRawBytes val bytes: Array<Byte>) {
        override fun hashCode(): Int = bytes.contentHashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BytesArray1) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }
    }

    @Serializable
    data class BytesArray2(@CborInfinite @CborRawBytes val bytes: Array<Byte>) {
        override fun hashCode(): Int = bytes.contentHashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BytesArray2) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }
    }

    @Serializable
    data class BytesList1(@CborRawBytes val bytes: List<Byte>)

    @Serializable
    data class BytesList2(@CborInfinite @CborRawBytes val bytes: List<Byte>)

    private fun cborInClass(cbor: ByteArray) = "A1656279746573".hex() + cbor

    @Test
    fun byteArray() {
        assertContentEquals(EMPTY, CBOR_EMPTY decodeCbor CborByteArraySerializer)
        assertContentEquals(CBOR_EMPTY, EMPTY encodeCbor CborByteArraySerializer)

        assertContentEquals(ONE, CBOR_ONE decodeCbor CborByteArraySerializer)
        assertContentEquals(CBOR_ONE, ONE encodeCbor CborByteArraySerializer)

        assertContentEquals(MAX_SIZE0, CBOR_MAX_SIZE0 decodeCbor CborByteArraySerializer)
        assertContentEquals(CBOR_MAX_SIZE0, MAX_SIZE0 encodeCbor CborByteArraySerializer)

        assertContentEquals(MIN_SIZE8, CBOR_MIN_SIZE8 decodeCbor CborByteArraySerializer)
        assertContentEquals(CBOR_MIN_SIZE8, MIN_SIZE8 encodeCbor CborByteArraySerializer)

        assertContentEquals(MAX_SIZE8, CBOR_MAX_SIZE8 decodeCbor CborByteArraySerializer)
        assertContentEquals(CBOR_MAX_SIZE8, MAX_SIZE8 encodeCbor CborByteArraySerializer)

        assertContentEquals(MIN_SIZE16, CBOR_MIN_SIZE16 decodeCbor CborByteArraySerializer)
        assertContentEquals(CBOR_MIN_SIZE16, MIN_SIZE16 encodeCbor CborByteArraySerializer)
    }

    @Test
    fun valueClassCborBytes() {
        assertContentEquals(CborBytes(EMPTY).bytes, CBOR_EMPTY.decodeCbor(serializer<CborBytes>()).bytes)
        assertContentEquals(CBOR_EMPTY, CborBytes(EMPTY) encodeCbor serializer())

        assertContentEquals(CborBytes(ONE).bytes, CBOR_ONE.decodeCbor(serializer<CborBytes>()).bytes)
        assertContentEquals(CBOR_ONE, CborBytes(ONE) encodeCbor serializer())

        assertContentEquals(CborBytes(MAX_SIZE0).bytes, CBOR_MAX_SIZE0.decodeCbor(serializer<CborBytes>()).bytes)
        assertContentEquals(CBOR_MAX_SIZE0, CborBytes(MAX_SIZE0) encodeCbor serializer())

        assertContentEquals(CborBytes(MIN_SIZE8).bytes, CBOR_MIN_SIZE8.decodeCbor(serializer<CborBytes>()).bytes)
        assertContentEquals(CBOR_MIN_SIZE8, CborBytes(MIN_SIZE8) encodeCbor serializer())

        assertContentEquals(CborBytes(MAX_SIZE8).bytes, CBOR_MAX_SIZE8.decodeCbor(serializer<CborBytes>()).bytes)
        assertContentEquals(CBOR_MAX_SIZE8, CborBytes(MAX_SIZE8) encodeCbor serializer())

        assertContentEquals(CborBytes(MIN_SIZE16).bytes, CBOR_MIN_SIZE16.decodeCbor(serializer<CborBytes>()).bytes)
        assertContentEquals(CBOR_MIN_SIZE16, CborBytes(MIN_SIZE16) encodeCbor serializer())
    }

    @Test
    fun listByteArray() {

        fun assertInfiniteEquals(data: List<ByteArray>, cbor: ByteArray) {
            val decoded = cbor decodeCbor CborListByteArraySerializer
            assertEquals(data.size, decoded.size)
            for (i in data.indices) assertContentEquals(data[i], decoded[i])
            assertContentEquals(cbor, data encodeCbor CborListByteArraySerializer)
        }

        assertInfiniteEquals(EMPTY.chunkedList(10), CBOR_EMPTY_INFINITE)
        assertInfiniteEquals(ONE.chunkedList(10), CBOR_ONE_INFINITE_x10)
        assertInfiniteEquals(MAX_SIZE0.chunkedList(10), CBOR_MAX_SIZE0_INFINITE_x10)
        assertInfiniteEquals(MIN_SIZE8.chunkedList(10), CBOR_MIN_SIZE8_INFINITE_x10)
        assertInfiniteEquals(MAX_SIZE8.chunkedList(10), CBOR_MAX_SIZE8_INFINITE_x10)
        assertInfiniteEquals(MIN_SIZE16.chunkedList(10), CBOR_MIN_SIZE16_INFINITE_x10)

        assertContentEquals(EMPTY, CBOR_EMPTY_INFINITE decodeCbor CborByteArraySerializer)
        assertContentEquals(ONE, CBOR_ONE_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MAX_SIZE0, CBOR_MAX_SIZE0_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MIN_SIZE8, CBOR_MIN_SIZE8_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MAX_SIZE8, CBOR_MAX_SIZE8_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MIN_SIZE16, CBOR_MIN_SIZE16_INFINITE_x10 decodeCbor CborByteArraySerializer)
    }

    @Test
    fun arrayByteArray() {

        fun assertInfiniteEquals(data: Array<ByteArray>, cbor: ByteArray) {
            val decoded = cbor decodeCbor CborArrayByteArraySerializer
            assertEquals(data.size, decoded.size)
            for (i in data.indices) assertContentEquals(data[i], decoded[i])
            assertContentEquals(cbor, data encodeCbor CborArrayByteArraySerializer)
        }

        assertInfiniteEquals(EMPTY.chunkedArray(10), CBOR_EMPTY_INFINITE)
        assertInfiniteEquals(ONE.chunkedArray(10), CBOR_ONE_INFINITE_x10)
        assertInfiniteEquals(MAX_SIZE0.chunkedArray(10), CBOR_MAX_SIZE0_INFINITE_x10)
        assertInfiniteEquals(MIN_SIZE8.chunkedArray(10), CBOR_MIN_SIZE8_INFINITE_x10)
        assertInfiniteEquals(MAX_SIZE8.chunkedArray(10), CBOR_MAX_SIZE8_INFINITE_x10)
        assertInfiniteEquals(MIN_SIZE16.chunkedArray(10), CBOR_MIN_SIZE16_INFINITE_x10)

        assertContentEquals(EMPTY, CBOR_EMPTY_INFINITE decodeCbor CborByteArraySerializer)
        assertContentEquals(ONE, CBOR_ONE_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MAX_SIZE0, CBOR_MAX_SIZE0_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MIN_SIZE8, CBOR_MIN_SIZE8_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MAX_SIZE8, CBOR_MAX_SIZE8_INFINITE_x10 decodeCbor CborByteArraySerializer)
        assertContentEquals(MIN_SIZE16, CBOR_MIN_SIZE16_INFINITE_x10 decodeCbor CborByteArraySerializer)
    }


    @Test
    fun classBytes1() {
        assertEquals(Bytes1(EMPTY), cborInClass(CBOR_EMPTY) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY), Bytes1(EMPTY) encodeCbor serializer())

        assertEquals(Bytes1(ONE), cborInClass(CBOR_ONE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_ONE), Bytes1(ONE) encodeCbor serializer())

        assertEquals(Bytes1(MAX_SIZE0), cborInClass(CBOR_MAX_SIZE0) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE0), Bytes1(MAX_SIZE0) encodeCbor serializer())

        assertEquals(Bytes1(MIN_SIZE8), cborInClass(CBOR_MIN_SIZE8) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE8), Bytes1(MIN_SIZE8) encodeCbor serializer())

        assertEquals(Bytes1(MAX_SIZE8), cborInClass(CBOR_MAX_SIZE8) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE8), Bytes1(MAX_SIZE8) encodeCbor serializer())

        assertEquals(Bytes1(MIN_SIZE16), cborInClass(CBOR_MIN_SIZE16) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE16), Bytes1(MIN_SIZE16) encodeCbor serializer())
    }

    @Test
    fun classBytes2() {
        assertEquals(Bytes2(EMPTY), cborInClass(CBOR_EMPTY_INFINITE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY_INFINITE), Bytes2(EMPTY) encodeCbor serializer())

        assertEquals(Bytes2(ONE), cborInClass(CBOR_ONE_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_ONE_INFINITE_x255), Bytes2(ONE) encodeCbor serializer())

        assertEquals(Bytes2(MAX_SIZE0), cborInClass(CBOR_MAX_SIZE0_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE0_INFINITE_x255), Bytes2(MAX_SIZE0) encodeCbor serializer())

        assertEquals(Bytes2(MIN_SIZE8), cborInClass(CBOR_MIN_SIZE8_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE8_INFINITE_x255), Bytes2(MIN_SIZE8) encodeCbor serializer())

        assertEquals(Bytes2(MAX_SIZE8), cborInClass(CBOR_MAX_SIZE8_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE8_INFINITE_x255), Bytes2(MAX_SIZE8) encodeCbor serializer())

        assertEquals(Bytes2(MIN_SIZE16), cborInClass(CBOR_MIN_SIZE16_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE16_INFINITE_x255), Bytes2(MIN_SIZE16) encodeCbor serializer())
    }

    @Test
    fun classBytesArray1() {
        assertEquals(BytesArray1(EMPTY.toTypedArray()), cborInClass(CBOR_EMPTY) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY), BytesArray1(EMPTY.toTypedArray()) encodeCbor serializer())

        assertEquals(BytesArray1(ONE.toTypedArray()), cborInClass(CBOR_ONE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_ONE), BytesArray1(ONE.toTypedArray()) encodeCbor serializer())

        assertEquals(BytesArray1(MAX_SIZE0.toTypedArray()), cborInClass(CBOR_MAX_SIZE0) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE0), BytesArray1(MAX_SIZE0.toTypedArray()) encodeCbor serializer())

        assertEquals(BytesArray1(MIN_SIZE8.toTypedArray()), cborInClass(CBOR_MIN_SIZE8) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE8), BytesArray1(MIN_SIZE8.toTypedArray()) encodeCbor serializer())

        assertEquals(BytesArray1(MAX_SIZE8.toTypedArray()), cborInClass(CBOR_MAX_SIZE8) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE8), BytesArray1(MAX_SIZE8.toTypedArray()) encodeCbor serializer())

        assertEquals(BytesArray1(MIN_SIZE16.toTypedArray()), cborInClass(CBOR_MIN_SIZE16) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE16),
            BytesArray1(MIN_SIZE16.toTypedArray()) encodeCbor serializer()
        )
    }

    @Test
    fun classBytesArray2() {
        assertEquals(BytesArray2(EMPTY.toTypedArray()), cborInClass(CBOR_EMPTY_INFINITE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY_INFINITE), BytesArray2(EMPTY.toTypedArray()) encodeCbor serializer())

        assertEquals(BytesArray2(ONE.toTypedArray()), cborInClass(CBOR_ONE_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_ONE_INFINITE_x255),
            BytesArray2(ONE.toTypedArray()) encodeCbor serializer()
        )

        assertEquals(
            BytesArray2(MAX_SIZE0.toTypedArray()),
            cborInClass(CBOR_MAX_SIZE0_INFINITE_x255) decodeCbor serializer()
        )
        assertContentEquals(
            cborInClass(CBOR_MAX_SIZE0_INFINITE_x255),
            BytesArray2(MAX_SIZE0.toTypedArray()) encodeCbor serializer()
        )

        assertEquals(
            BytesArray2(MIN_SIZE8.toTypedArray()),
            cborInClass(CBOR_MIN_SIZE8_INFINITE_x255) decodeCbor serializer()
        )
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE8_INFINITE_x255),
            BytesArray2(MIN_SIZE8.toTypedArray()) encodeCbor serializer()
        )

        assertEquals(
            BytesArray2(MAX_SIZE8.toTypedArray()),
            cborInClass(CBOR_MAX_SIZE8_INFINITE_x255) decodeCbor serializer()
        )
        assertContentEquals(
            cborInClass(CBOR_MAX_SIZE8_INFINITE_x255),
            BytesArray2(MAX_SIZE8.toTypedArray()) encodeCbor serializer()
        )

        assertEquals(
            BytesArray2(MIN_SIZE16.toTypedArray()),
            cborInClass(CBOR_MIN_SIZE16_INFINITE_x255) decodeCbor serializer()
        )
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE16_INFINITE_x255),
            BytesArray2(MIN_SIZE16.toTypedArray()) encodeCbor serializer()
        )
    }

    @Test
    fun classBytesList1() {
        assertEquals(BytesList1(EMPTY.toList()), cborInClass(CBOR_EMPTY) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY), BytesList1(EMPTY.toList()) encodeCbor serializer())

        assertEquals(BytesList1(ONE.toList()), cborInClass(CBOR_ONE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_ONE), BytesList1(ONE.toList()) encodeCbor serializer())

        assertEquals(BytesList1(MAX_SIZE0.toList()), cborInClass(CBOR_MAX_SIZE0) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE0), BytesList1(MAX_SIZE0.toList()) encodeCbor serializer())

        assertEquals(BytesList1(MIN_SIZE8.toList()), cborInClass(CBOR_MIN_SIZE8) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE8), BytesList1(MIN_SIZE8.toList()) encodeCbor serializer())

        assertEquals(BytesList1(MAX_SIZE8.toList()), cborInClass(CBOR_MAX_SIZE8) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MAX_SIZE8), BytesList1(MAX_SIZE8.toList()) encodeCbor serializer())

        assertEquals(BytesList1(MIN_SIZE16.toList()), cborInClass(CBOR_MIN_SIZE16) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_MIN_SIZE16), BytesList1(MIN_SIZE16.toList()) encodeCbor serializer())
    }

    @Test
    fun classBytesList2() {
        assertEquals(BytesList2(EMPTY.toList()), cborInClass(CBOR_EMPTY_INFINITE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY_INFINITE), BytesList2(EMPTY.toList()) encodeCbor serializer())

        assertEquals(BytesList2(ONE.toList()), cborInClass(CBOR_ONE_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_ONE_INFINITE_x255), BytesList2(ONE.toList()) encodeCbor serializer())

        assertEquals(BytesList2(MAX_SIZE0.toList()), cborInClass(CBOR_MAX_SIZE0_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MAX_SIZE0_INFINITE_x255),
            BytesList2(MAX_SIZE0.toList()) encodeCbor serializer()
        )

        assertEquals(BytesList2(MIN_SIZE8.toList()), cborInClass(CBOR_MIN_SIZE8_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE8_INFINITE_x255),
            BytesList2(MIN_SIZE8.toList()) encodeCbor serializer()
        )

        assertEquals(BytesList2(MAX_SIZE8.toList()), cborInClass(CBOR_MAX_SIZE8_INFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MAX_SIZE8_INFINITE_x255),
            BytesList2(MAX_SIZE8.toList()) encodeCbor serializer()
        )

        assertEquals(
            BytesList2(MIN_SIZE16.toList()),
            cborInClass(CBOR_MIN_SIZE16_INFINITE_x255) decodeCbor serializer()
        )
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE16_INFINITE_x255),
            BytesList2(MIN_SIZE16.toList()) encodeCbor serializer()
        )
    }
}