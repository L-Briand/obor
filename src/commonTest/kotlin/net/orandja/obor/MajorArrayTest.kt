package net.orandja.obor

import kotlinx.serialization.serializer
import net.orandja.obor.codec.*
import kotlin.experimental.or
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MajorArrayTest {
    companion object {
        val INT_EMPTY = Array(0) { 0 }
        val CBOR_INT_EMPTY = buildSize(MAJOR_ARRAY, 0)
        val CBOR_INT_EMPTY_INDEFINITE = buildIndefinite(MAJOR_ARRAY, 0) { write(0) }


        val INT_ONE = Array(1) { 0 }
        val CBOR_INT_ONE = buildSize(MAJOR_ARRAY, 1)
        val CBOR_INT_ONE_INDEFINITE = buildIndefinite(MAJOR_ARRAY, 1) { write(0) }


        val INT_MAX_SIZE0 = Array(0x17) { 0 }
        val CBOR_INT_MAX_SIZE0 = buildSize(MAJOR_ARRAY, 0x17)
        val CBOR_INT_MAX_SIZE0_INDEFINITE = buildIndefinite(MAJOR_ARRAY, 0x17) { write(0) }


        val INT_MIN_SIZE8 = Array(0x18) { 0 }
        val CBOR_INT_MIN_SIZE8 = buildSize(MAJOR_ARRAY, 0x18)
        val CBOR_INT_MIN_SIZE8_INDEFINITE = buildIndefinite(MAJOR_ARRAY, 0x18) { write(0) }


        val INT_MAX_SIZE8 = Array(0xFF) { 0 }
        val CBOR_INT_MAX_SIZE8 = buildSize(MAJOR_ARRAY, 0xFF)
        val CBOR_INT_MAX_SIZE8_INDEFINITE = buildIndefinite(MAJOR_ARRAY, 0xFF) { write(0) }


        val INT_MIN_SIZE16 = Array(0x100) { 0 }
        val CBOR_INT_MIN_SIZE16 = buildSize(MAJOR_ARRAY, 0x100)
        val CBOR_INT_MIN_SIZE16_INDEFINITE = buildIndefinite(MAJOR_ARRAY, 0x100) { write(0) }


        val PRIMITIVE_BOOLEAN = BooleanArray(1) { false }
        val CBOR_PRIMITIVE_BOOLEAN = buildCbor { writeMajor8(MAJOR_ARRAY, 1); write(HEADER_FALSE) }
        val PRIMITIVE_BYTE = ByteArray(1) { 0 }
        val CBOR_PRIMITIVE_BYTE = buildCbor { writeMajor8(MAJOR_ARRAY, 1); write(0) }
        val PRIMITIVE_SHORT = ShortArray(1) { 0 }
        val CBOR_PRIMITIVE_SHORT = buildCbor { writeMajor8(MAJOR_ARRAY, 1); write(0) }
        val PRIMITIVE_INT = IntArray(1) { 0 }
        val CBOR_PRIMITIVE_INT = buildCbor { writeMajor8(MAJOR_ARRAY, 1); write(0) }
        val PRIMITIVE_LONG = LongArray(1) { 0 }
        val CBOR_PRIMITIVE_LONG = buildCbor { writeMajor8(MAJOR_ARRAY, 1); write(0) }
        val PRIMITIVE_CHAR = CharArray(1) { 'a' }
        val CBOR_PRIMITIVE_CHAR = buildCbor {
            writeMajor8(MAJOR_ARRAY, 1)
            writeHeader8(MAJOR_TEXT or 1, 'a'.code.toByte())
        }
        val PRIMITIVE_FLOAT = FloatArray(1) { 0.0f }
        val CBOR_PRIMITIVE_FLOAT = buildCbor {
            writeMajor8(MAJOR_ARRAY, 1)
            writeHeader16(HEADER_FLOAT_16, float32ToFloat16bits(0.0f).toShort())
        }
        val PRIMITIVE_DOUBLE = DoubleArray(1) { 0.0 }
        val CBOR_PRIMITIVE_DOUBLE = buildCbor {
            writeMajor8(MAJOR_ARRAY, 1)
            writeHeader16(HEADER_FLOAT_16, float32ToFloat16bits(0.0f).toShort())
        }
    }

    @Test
    fun arrays() {
        assertContentEquals(INT_EMPTY, CBOR_INT_EMPTY decodeCbor serializer())
        assertContentEquals(CBOR_INT_EMPTY, INT_EMPTY encodeCbor serializer())

        assertContentEquals(INT_ONE, CBOR_INT_ONE decodeCbor serializer())
        assertContentEquals(CBOR_INT_ONE, INT_ONE encodeCbor serializer())

        assertContentEquals(INT_MAX_SIZE0, CBOR_INT_MAX_SIZE0 decodeCbor serializer())
        assertContentEquals(CBOR_INT_MAX_SIZE0, INT_MAX_SIZE0 encodeCbor serializer())

        assertContentEquals(INT_MIN_SIZE8, CBOR_INT_MIN_SIZE8 decodeCbor serializer())
        assertContentEquals(CBOR_INT_MIN_SIZE8, INT_MIN_SIZE8 encodeCbor serializer())

        assertContentEquals(INT_MAX_SIZE8, CBOR_INT_MAX_SIZE8 decodeCbor serializer())
        assertContentEquals(CBOR_INT_MAX_SIZE8, INT_MAX_SIZE8 encodeCbor serializer())

        assertContentEquals(INT_MIN_SIZE16, CBOR_INT_MIN_SIZE16 decodeCbor serializer())
        assertContentEquals(CBOR_INT_MIN_SIZE16, INT_MIN_SIZE16 encodeCbor serializer())
    }

    @Test
    fun arrayIndefinite() {
        assertContentEquals(
            Indefinite(INT_EMPTY).array,
            (CBOR_INT_EMPTY_INDEFINITE decodeCbor serializer<Indefinite<Array<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_EMPTY_INDEFINITE,
            Indefinite(INT_EMPTY) encodeCbor serializer<Indefinite<Array<Int>>>()
        )

        assertContentEquals(
            Indefinite(INT_ONE).array,
            (CBOR_INT_ONE_INDEFINITE decodeCbor serializer<Indefinite<Array<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_ONE_INDEFINITE,
            Indefinite(INT_ONE) encodeCbor serializer<Indefinite<Array<Int>>>()
        )

        assertContentEquals(
            Indefinite(INT_MAX_SIZE0).array,
            (CBOR_INT_MAX_SIZE0_INDEFINITE decodeCbor serializer<Indefinite<Array<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MAX_SIZE0_INDEFINITE,
            Indefinite(INT_MAX_SIZE0) encodeCbor serializer<Indefinite<Array<Int>>>()
        )

        assertContentEquals(
            Indefinite(INT_MIN_SIZE8).array,
            (CBOR_INT_MIN_SIZE8_INDEFINITE decodeCbor serializer<Indefinite<Array<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MIN_SIZE8_INDEFINITE,
            Indefinite(INT_MIN_SIZE8) encodeCbor serializer<Indefinite<Array<Int>>>()
        )

        assertContentEquals(
            Indefinite(INT_MAX_SIZE8).array,
            (CBOR_INT_MAX_SIZE8_INDEFINITE decodeCbor serializer<Indefinite<Array<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MAX_SIZE8_INDEFINITE,
            Indefinite(INT_MAX_SIZE8) encodeCbor serializer<Indefinite<Array<Int>>>()
        )

        assertContentEquals(
            Indefinite(INT_MIN_SIZE16).array,
            (CBOR_INT_MIN_SIZE16_INDEFINITE decodeCbor serializer<Indefinite<Array<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MIN_SIZE16_INDEFINITE,
            Indefinite(INT_MIN_SIZE16) encodeCbor serializer<Indefinite<Array<Int>>>()
        )

    }

    @Test
    fun decodeArrayIndefiniteWithArraySerializer() {
        assertContentEquals(INT_EMPTY, CBOR_INT_EMPTY_INDEFINITE decodeCbor serializer())
        assertContentEquals(INT_ONE, CBOR_INT_ONE_INDEFINITE decodeCbor serializer())
        assertContentEquals(INT_MAX_SIZE0, CBOR_INT_MAX_SIZE0_INDEFINITE decodeCbor serializer())
        assertContentEquals(INT_MIN_SIZE8, CBOR_INT_MIN_SIZE8_INDEFINITE decodeCbor serializer())
        assertContentEquals(INT_MAX_SIZE8, CBOR_INT_MAX_SIZE8_INDEFINITE decodeCbor serializer())
        assertContentEquals(INT_MIN_SIZE16, CBOR_INT_MIN_SIZE16_INDEFINITE decodeCbor serializer())
    }

    @Test
    fun lists() {
        assertContentEquals(INT_EMPTY.toList(), CBOR_INT_EMPTY decodeCbor serializer<List<Int>>())
        assertContentEquals(CBOR_INT_EMPTY, INT_EMPTY.toList() encodeCbor serializer<List<Int>>())

        assertContentEquals(INT_ONE.toList(), CBOR_INT_ONE decodeCbor serializer<List<Int>>())
        assertContentEquals(CBOR_INT_ONE, INT_ONE.toList() encodeCbor serializer<List<Int>>())

        assertContentEquals(INT_MAX_SIZE0.toList(), CBOR_INT_MAX_SIZE0 decodeCbor serializer<List<Int>>())
        assertContentEquals(CBOR_INT_MAX_SIZE0, INT_MAX_SIZE0.toList() encodeCbor serializer<List<Int>>())

        assertContentEquals(INT_MIN_SIZE8.toList(), CBOR_INT_MIN_SIZE8 decodeCbor serializer<List<Int>>())
        assertContentEquals(CBOR_INT_MIN_SIZE8, INT_MIN_SIZE8.toList() encodeCbor serializer<List<Int>>())

        assertContentEquals(INT_MAX_SIZE8.toList(), CBOR_INT_MAX_SIZE8 decodeCbor serializer<List<Int>>())
        assertContentEquals(CBOR_INT_MAX_SIZE8, INT_MAX_SIZE8.toList() encodeCbor serializer<List<Int>>())

        assertContentEquals(INT_MIN_SIZE16.toList(), CBOR_INT_MIN_SIZE16 decodeCbor serializer<List<Int>>())
        assertContentEquals(CBOR_INT_MIN_SIZE16, INT_MIN_SIZE16.toList() encodeCbor serializer<List<Int>>())
    }

    @Test
    fun listIndefinite() {
        assertEquals(
            Indefinite(INT_EMPTY.toList()).array,
            (CBOR_INT_EMPTY_INDEFINITE decodeCbor serializer<Indefinite<List<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_EMPTY_INDEFINITE,
            Indefinite(INT_EMPTY.toList()) encodeCbor serializer<Indefinite<List<Int>>>()
        )

        assertEquals(
            Indefinite(INT_ONE.toList()).array,
            (CBOR_INT_ONE_INDEFINITE decodeCbor serializer<Indefinite<List<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_ONE_INDEFINITE,
            Indefinite(INT_ONE.toList()) encodeCbor serializer<Indefinite<List<Int>>>()
        )

        assertEquals(
            Indefinite(INT_MAX_SIZE0.toList()).array,
            (CBOR_INT_MAX_SIZE0_INDEFINITE decodeCbor serializer<Indefinite<List<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MAX_SIZE0_INDEFINITE,
            Indefinite(INT_MAX_SIZE0.toList()) encodeCbor serializer<Indefinite<List<Int>>>()
        )

        assertEquals(
            Indefinite(INT_MIN_SIZE8.toList()).array,
            (CBOR_INT_MIN_SIZE8_INDEFINITE decodeCbor serializer<Indefinite<List<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MIN_SIZE8_INDEFINITE,
            Indefinite(INT_MIN_SIZE8.toList()) encodeCbor serializer<Indefinite<List<Int>>>()
        )

        assertEquals(
            Indefinite(INT_MAX_SIZE8.toList()).array,
            (CBOR_INT_MAX_SIZE8_INDEFINITE decodeCbor serializer<Indefinite<List<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MAX_SIZE8_INDEFINITE,
            Indefinite(INT_MAX_SIZE8.toList()) encodeCbor serializer<Indefinite<List<Int>>>()
        )

        assertEquals(
            Indefinite(INT_MIN_SIZE16.toList()).array,
            (CBOR_INT_MIN_SIZE16_INDEFINITE decodeCbor serializer<Indefinite<List<Int>>>()).array
        )
        assertContentEquals(
            CBOR_INT_MIN_SIZE16_INDEFINITE,
            Indefinite(INT_MIN_SIZE16.toList()) encodeCbor serializer<Indefinite<List<Int>>>()
        )

    }

    @Test
    fun decodeListIndefiniteWithListSerializer() {
        assertContentEquals(INT_EMPTY.toList(), CBOR_INT_EMPTY_INDEFINITE decodeCbor serializer<List<Int>>())
        assertContentEquals(INT_ONE.toList(), CBOR_INT_ONE_INDEFINITE decodeCbor serializer<List<Int>>())
        assertContentEquals(INT_MAX_SIZE0.toList(), CBOR_INT_MAX_SIZE0_INDEFINITE decodeCbor serializer<List<Int>>())
        assertContentEquals(INT_MIN_SIZE8.toList(), CBOR_INT_MIN_SIZE8_INDEFINITE decodeCbor serializer<List<Int>>())
        assertContentEquals(INT_MAX_SIZE8.toList(), CBOR_INT_MAX_SIZE8_INDEFINITE decodeCbor serializer<List<Int>>())
        assertContentEquals(INT_MIN_SIZE16.toList(), CBOR_INT_MIN_SIZE16_INDEFINITE decodeCbor serializer<List<Int>>())
    }

    @Test
    fun primitives() {
        assertContentEquals(PRIMITIVE_BOOLEAN, CBOR_PRIMITIVE_BOOLEAN decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_BOOLEAN, PRIMITIVE_BOOLEAN encodeCbor serializer())

        assertContentEquals(PRIMITIVE_BYTE, CBOR_PRIMITIVE_BYTE decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_BYTE, PRIMITIVE_BYTE encodeCbor serializer())

        assertContentEquals(PRIMITIVE_SHORT, CBOR_PRIMITIVE_SHORT decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_SHORT, PRIMITIVE_SHORT encodeCbor serializer())

        assertContentEquals(PRIMITIVE_INT, CBOR_PRIMITIVE_INT decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_INT, PRIMITIVE_INT encodeCbor serializer())

        assertContentEquals(PRIMITIVE_LONG, CBOR_PRIMITIVE_LONG decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_LONG, PRIMITIVE_LONG encodeCbor serializer())

        assertContentEquals(PRIMITIVE_CHAR, CBOR_PRIMITIVE_CHAR decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_CHAR, PRIMITIVE_CHAR encodeCbor serializer())

        assertContentEquals(PRIMITIVE_FLOAT, CBOR_PRIMITIVE_FLOAT decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_FLOAT, PRIMITIVE_FLOAT encodeCbor serializer())

        assertContentEquals(PRIMITIVE_DOUBLE, CBOR_PRIMITIVE_DOUBLE decodeCbor serializer())
        assertContentEquals(CBOR_PRIMITIVE_DOUBLE, PRIMITIVE_DOUBLE encodeCbor serializer())
    }
}