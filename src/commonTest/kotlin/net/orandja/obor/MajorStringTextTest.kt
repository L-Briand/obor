package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.orandja.obor.annotations.CborIndefinite
import net.orandja.obor.codec.MAJOR_TEXT
import net.orandja.obor.serializer.CborArrayStringSerializer
import net.orandja.obor.serializer.CborListStringSerializer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MajorStringTextTest {
    companion object {
        // 0 ITEM
        val EMPTY = ""
        val CBOR_EMPTY = buildCbor { writeMajor8(MAJOR_TEXT, 0) }
        val CBOR_EMPTY_INDEFINITE = buildChunkedIndefinite(MAJOR_TEXT, 0, 10, 'a'.code.toByte())

        // 1 ITEM
        val ONE = "a"
        val CBOR_ONE = buildSize(MAJOR_TEXT, 1, 'a'.code.toByte())
        val CBOR_ONE_INDEFINITE_x10 = buildChunkedIndefinite(MAJOR_TEXT, 1, 10, 'a'.code.toByte())
        val CBOR_ONE_INDEFINITE_x255 = buildChunkedIndefinite(MAJOR_TEXT, 1, 255, 'a'.code.toByte())

        // 23 ITEMS
        val MAX_SIZE0 = buildString { repeat(0x17) { append('a') } }
        val CBOR_MAX_SIZE0 = buildSize(MAJOR_TEXT, 0x17, 'a'.code.toByte())
        val CBOR_MAX_SIZE0_INDEFINITE_x10 = buildChunkedIndefinite(MAJOR_TEXT, 0x17, 10, 'a'.code.toByte())
        val CBOR_MAX_SIZE0_INDEFINITE_x255 = buildChunkedIndefinite(MAJOR_TEXT, 0x17, 255, 'a'.code.toByte())

        // 24 ITEMS
        val MIN_SIZE8 = buildString { repeat(0x18) { append('a') } }
        val CBOR_MIN_SIZE8 = buildSize(MAJOR_TEXT, 0x18, 'a'.code.toByte())
        val CBOR_MIN_SIZE8_INDEFINITE_x10 = buildChunkedIndefinite(MAJOR_TEXT, 0x18, 10, 'a'.code.toByte())
        val CBOR_MIN_SIZE8_INDEFINITE_x255 = buildChunkedIndefinite(MAJOR_TEXT, 0x18, 255, 'a'.code.toByte())

        // 255 ITEMS
        val MAX_SIZE8 = buildString { repeat(0xFF) { append('a') } }
        val CBOR_MAX_SIZE8 = buildSize(MAJOR_TEXT, 0xFF, 'a'.code.toByte())
        val CBOR_MAX_SIZE8_INDEFINITE_x10 = buildChunkedIndefinite(MAJOR_TEXT, 0xFF, 10, 'a'.code.toByte())
        val CBOR_MAX_SIZE8_INDEFINITE_x255 = buildChunkedIndefinite(MAJOR_TEXT, 0xFF, 0xFF, 'a'.code.toByte())

        // 257 ITEMS
        val MIN_SIZE16 = buildString { repeat(0x100) { append('a') } }
        val CBOR_MIN_SIZE16 = buildSize(MAJOR_TEXT, 0x100, 'a'.code.toByte())
        val CBOR_MIN_SIZE16_INDEFINITE_x10 = buildChunkedIndefinite(MAJOR_TEXT, 0x100, 10, 'a'.code.toByte())
        val CBOR_MIN_SIZE16_INDEFINITE_x255 = buildChunkedIndefinite(MAJOR_TEXT, 0x100, 0xFF, 'a'.code.toByte())
    }

    private fun String.chunkedList(amount: Int) = chunked(amount)
    private fun String.chunkedArray(amount: Int) = chunked(amount).toTypedArray()

    @Test
    fun string() {
        assertEquals(EMPTY, CBOR_EMPTY decodeCbor serializer())
        assertContentEquals(CBOR_EMPTY, EMPTY encodeCbor serializer())

        assertEquals(ONE, CBOR_ONE decodeCbor serializer())
        assertContentEquals(CBOR_ONE, ONE encodeCbor serializer())

        assertEquals(MAX_SIZE0, CBOR_MAX_SIZE0 decodeCbor serializer())
        assertContentEquals(CBOR_MAX_SIZE0, MAX_SIZE0 encodeCbor serializer())

        assertEquals(MIN_SIZE8, CBOR_MIN_SIZE8 decodeCbor serializer())
        assertContentEquals(CBOR_MIN_SIZE8, MIN_SIZE8 encodeCbor serializer())

        assertEquals(MAX_SIZE8, CBOR_MAX_SIZE8 decodeCbor serializer())
        assertContentEquals(CBOR_MAX_SIZE8, MAX_SIZE8 encodeCbor serializer())

        assertEquals(MIN_SIZE16, CBOR_MIN_SIZE16 decodeCbor serializer())
        assertContentEquals(CBOR_MIN_SIZE16, MIN_SIZE16 encodeCbor serializer())
    }

    @Test
    fun listString() {

        fun assertIndefiniteEquals(data: List<String>, cbor: ByteArray) {
            val decoded = cbor decodeCbor CborListStringSerializer
            assertEquals(data.size, decoded.size)
            for (i in data.indices) assertEquals(data[i], decoded[i])
            assertContentEquals(cbor, data encodeCbor CborListStringSerializer)
        }

        assertIndefiniteEquals(EMPTY.chunkedList(10), CBOR_EMPTY_INDEFINITE)
        assertIndefiniteEquals(ONE.chunkedList(10), CBOR_ONE_INDEFINITE_x10)
        assertIndefiniteEquals(MAX_SIZE0.chunkedList(10), CBOR_MAX_SIZE0_INDEFINITE_x10)
        assertIndefiniteEquals(MIN_SIZE8.chunkedList(10), CBOR_MIN_SIZE8_INDEFINITE_x10)
        assertIndefiniteEquals(MAX_SIZE8.chunkedList(10), CBOR_MAX_SIZE8_INDEFINITE_x10)
        assertIndefiniteEquals(MIN_SIZE16.chunkedList(10), CBOR_MIN_SIZE16_INDEFINITE_x10)

        assertEquals(EMPTY, CBOR_EMPTY_INDEFINITE decodeCbor serializer())
        assertEquals(ONE, CBOR_ONE_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MAX_SIZE0, CBOR_MAX_SIZE0_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MIN_SIZE8, CBOR_MIN_SIZE8_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MAX_SIZE8, CBOR_MAX_SIZE8_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MIN_SIZE16, CBOR_MIN_SIZE16_INDEFINITE_x10 decodeCbor serializer())
    }

    @Test
    fun arrayString() {

        fun assertIndefiniteEquals(data: Array<String>, cbor: ByteArray) {
            val decoded = cbor decodeCbor CborArrayStringSerializer
            assertEquals(data.size, decoded.size)
            for (i in data.indices) assertEquals(data[i], decoded[i])
            assertContentEquals(cbor, data encodeCbor CborArrayStringSerializer)
        }

        assertIndefiniteEquals(EMPTY.chunkedArray(10), CBOR_EMPTY_INDEFINITE)
        assertIndefiniteEquals(ONE.chunkedArray(10), CBOR_ONE_INDEFINITE_x10)
        assertIndefiniteEquals(MAX_SIZE0.chunkedArray(10), CBOR_MAX_SIZE0_INDEFINITE_x10)
        assertIndefiniteEquals(MIN_SIZE8.chunkedArray(10), CBOR_MIN_SIZE8_INDEFINITE_x10)
        assertIndefiniteEquals(MAX_SIZE8.chunkedArray(10), CBOR_MAX_SIZE8_INDEFINITE_x10)
        assertIndefiniteEquals(MIN_SIZE16.chunkedArray(10), CBOR_MIN_SIZE16_INDEFINITE_x10)

        assertEquals(EMPTY, CBOR_EMPTY_INDEFINITE decodeCbor serializer())
        assertEquals(ONE, CBOR_ONE_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MAX_SIZE0, CBOR_MAX_SIZE0_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MIN_SIZE8, CBOR_MIN_SIZE8_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MAX_SIZE8, CBOR_MAX_SIZE8_INDEFINITE_x10 decodeCbor serializer())
        assertEquals(MIN_SIZE16, CBOR_MIN_SIZE16_INDEFINITE_x10 decodeCbor serializer())
    }

    @Test
    fun inlineIndefiniteString() {
        assertEquals(Indefinite(EMPTY), CBOR_EMPTY_INDEFINITE decodeCbor serializer())
        assertContentEquals(CBOR_EMPTY_INDEFINITE, Indefinite(EMPTY) encodeCbor serializer())

        assertEquals(Indefinite(ONE), CBOR_ONE_INDEFINITE_x255 decodeCbor serializer())
        assertContentEquals(CBOR_ONE_INDEFINITE_x255, Indefinite(ONE) encodeCbor serializer())

        assertEquals(Indefinite(MAX_SIZE0), CBOR_MAX_SIZE0_INDEFINITE_x255 decodeCbor serializer())
        assertContentEquals(CBOR_MAX_SIZE0_INDEFINITE_x255, Indefinite(MAX_SIZE0) encodeCbor serializer())

        assertEquals(Indefinite(MIN_SIZE8), CBOR_MIN_SIZE8_INDEFINITE_x255 decodeCbor serializer())
        assertContentEquals(CBOR_MIN_SIZE8_INDEFINITE_x255, Indefinite(MIN_SIZE8) encodeCbor serializer())

        assertEquals(Indefinite(MAX_SIZE8), CBOR_MAX_SIZE8_INDEFINITE_x255 decodeCbor serializer())
        assertContentEquals(CBOR_MAX_SIZE8_INDEFINITE_x255, Indefinite(MAX_SIZE8) encodeCbor serializer())

        assertEquals(Indefinite(MIN_SIZE16), CBOR_MIN_SIZE16_INDEFINITE_x255 decodeCbor serializer())
        assertContentEquals(CBOR_MIN_SIZE16_INDEFINITE_x255, Indefinite(MIN_SIZE16) encodeCbor serializer())
    }


    @Serializable
    data class IndefiniteString(@CborIndefinite val string: String)

    private fun cborInClass(cbor: ByteArray) = "A166737472696E67".hex() + cbor

    @Test
    fun indefiniteString() {
        assertEquals(IndefiniteString(EMPTY), cborInClass(CBOR_EMPTY_INDEFINITE) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_EMPTY_INDEFINITE), IndefiniteString(EMPTY) encodeCbor serializer())

        assertEquals(IndefiniteString(ONE), cborInClass(CBOR_ONE_INDEFINITE_x255) decodeCbor serializer())
        assertContentEquals(cborInClass(CBOR_ONE_INDEFINITE_x255), IndefiniteString(ONE) encodeCbor serializer())

        assertEquals(IndefiniteString(MAX_SIZE0), cborInClass(CBOR_MAX_SIZE0_INDEFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MAX_SIZE0_INDEFINITE_x255),
            IndefiniteString(MAX_SIZE0) encodeCbor serializer()
        )

        assertEquals(IndefiniteString(MIN_SIZE8), cborInClass(CBOR_MIN_SIZE8_INDEFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE8_INDEFINITE_x255),
            IndefiniteString(MIN_SIZE8) encodeCbor serializer()
        )

        assertEquals(IndefiniteString(MAX_SIZE8), cborInClass(CBOR_MAX_SIZE8_INDEFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MAX_SIZE8_INDEFINITE_x255),
            IndefiniteString(MAX_SIZE8) encodeCbor serializer()
        )

        assertEquals(IndefiniteString(MIN_SIZE16), cborInClass(CBOR_MIN_SIZE16_INDEFINITE_x255) decodeCbor serializer())
        assertContentEquals(
            cborInClass(CBOR_MIN_SIZE16_INDEFINITE_x255),
            IndefiniteString(MIN_SIZE16) encodeCbor serializer()
        )
    }
}