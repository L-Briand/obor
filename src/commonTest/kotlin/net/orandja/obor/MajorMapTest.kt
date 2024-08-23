package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.codec.MAJOR_MAP
import net.orandja.obor.codec.MAJOR_POSITIVE
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MajorMapTest {

    @Test
    fun emptyMap() {
        val CBOR_MAP_EMPTY = buildCbor { writeMajor8(MAJOR_MAP, 0) }
        assertContentEquals(CBOR_MAP_EMPTY, emptyMap<Int, String>() encodeCbor serializer())
        assertEquals(emptyMap<Int, String>(), CBOR_MAP_EMPTY decodeCbor serializer<Map<Int, String>>())
        assertContentEquals(CBOR_MAP_EMPTY, emptyMap<Float, Boolean>() encodeCbor serializer())
        assertEquals(emptyMap<Float, Boolean>(), CBOR_MAP_EMPTY decodeCbor serializer<Map<Float, Boolean>>())
        assertContentEquals(CBOR_MAP_EMPTY, emptyMap<Int, String>() encodeCbor serializer())
        assertEquals(emptyMap<Int, String>(), CBOR_MAP_EMPTY decodeCbor serializer<Map<Int, String>>())
        assertContentEquals(CBOR_MAP_EMPTY, emptyMap<UShort, Array<String>>() encodeCbor serializer())
        assertEquals(
            emptyMap<UShort, Array<String>>(),
            CBOR_MAP_EMPTY decodeCbor serializer<Map<UShort, Array<String>>>()
        )
        assertContentEquals(CBOR_MAP_EMPTY, emptyMap<Long, Infinite<String>>() encodeCbor serializer())
        assertEquals(
            emptyMap<Long, Infinite<String>>(),
            CBOR_MAP_EMPTY decodeCbor serializer<Map<Long, Infinite<String>>>()
        )
    }

    @Test
    fun intTesting() {
        val MAP_INT = mapOf(0xFF.toLong() to 0xFFFF.toLong(), 0xFFFF_FFFF to 0x1FFF_FFFF_FFFF_FFFF)
        val CBOR_MAP_INT = buildCbor {
            writeMajor8(MAJOR_MAP, 2)
            writeMajor64(MAJOR_POSITIVE, 0xFF)
            writeMajor64(MAJOR_POSITIVE, 0xFFFF)
            writeMajor64(MAJOR_POSITIVE, 0xFFFF_FFFF)
            writeMajor64(MAJOR_POSITIVE, 0x1FFF_FFFF_FFFF_FFFF)
        }

        run {
            assertContentEquals(CBOR_MAP_INT, MAP_INT encodeCbor serializer())
            val decoded = CBOR_MAP_INT decodeCbor serializer<Map<Long, Long>>()
            assertEquals(MAP_INT.size, decoded.size)
            for ((key, value) in MAP_INT) {
                assertTrue(key in decoded.keys)
                assertTrue(value in decoded.values)
                assertEquals(value, decoded[key])
            }
        }

        run {
            assertContentEquals(CBOR_MAP_INT, MAP_INT encodeCbor serializer())
            val decoded = CBOR_MAP_INT decodeCbor serializer<Map<Long, Long>>()
            assertEquals(MAP_INT.size, decoded.size)
            for ((key, value) in MAP_INT) {
                assertTrue(key in decoded.keys)
                assertTrue(value in decoded.values)
                assertEquals(value, decoded[key])
            }
        }
    }

    @Serializable
    data class User(val id: Int, val name: String)

    @Serializable
    @CborInfinite
    data class UserInfinite(val id: Int, val name: String)

    @Test
    fun basicDataClass() {
        val user = User(0, "John")
        val cborUser = "A262696400646E616D65644A6F686E".hex()
        val userInf = UserInfinite(0, "John")
        val cborUserInf = "BF62696400646E616D65644A6F686EFF".hex()

        assertEquals(user, cborUser decodeCbor serializer())
        assertContentEquals(cborUser, user encodeCbor serializer())

        assertEquals(userInf, cborUserInf decodeCbor serializer())
        assertContentEquals(cborUserInf, userInf encodeCbor serializer())
    }
}