package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.decoder.CborDecoderException
import kotlin.jvm.JvmInline
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CborTagTest {
    // @formatter:off
    @Serializable @CborTag(0x17)               data class Id00(@CborTag(0x17)               val id: Int)
    @Serializable @CborTag(0x80)               data class Id08(@CborTag(0x80)               val id: Int)
    @Serializable @CborTag(0x8000)             data class Id16(@CborTag(0x8000)             val id: Int)
    @Serializable @CborTag(0x80000000L)        data class Id32(@CborTag(0x80000000L)        val id: Int)
    @Serializable @CborTag(0x1000000000000000) data class Id64(@CborTag(0x1000000000000000) val id: Int)
    @Serializable @CborTag(-1)                 data class IdFF(@CborTag(-1)                 val id: Int)
    // @formatter:on

    @Test
    fun tagSize() {
        val cborNoTag = "A162696400".hex()

        val id00 = Id00(0)
        val cborId00 = "D7A1626964D700".hex()
        assertContentEquals(cborId00, id00 encodeCbor serializer())
        assertEquals(id00, cborId00 decodeCbor serializer())
        assertEquals(id00, cborNoTag decodeCbor serializer())

        val id08 = Id08(0)
        val cborId08 = "D880A1626964D88000".hex()
        assertContentEquals(cborId08, id08 encodeCbor serializer())
        assertEquals(id08, cborId08 decodeCbor serializer())
        assertEquals(id08, cborNoTag decodeCbor serializer())

        val id16 = Id16(0)
        val cborId16 = "D98000A1626964D9800000".hex()
        assertContentEquals(cborId16, id16 encodeCbor serializer())
        assertEquals(id16, cborId16 decodeCbor serializer())
        assertEquals(id16, cborNoTag decodeCbor serializer())

        val id32 = Id32(0)
        val cborId32 = "DA80000000A1626964DA8000000000".hex()
        assertContentEquals(cborId32, id32 encodeCbor serializer())
        assertEquals(id32, cborId32 decodeCbor serializer())
        assertEquals(id32, cborNoTag decodeCbor serializer())

        val id64 = Id64(0)
        val cborId64 = "DB1000000000000000A1626964DB100000000000000000".hex()
        assertContentEquals(cborId64, id64 encodeCbor serializer())
        assertEquals(id64, cborId64 decodeCbor serializer())
        assertEquals(id64, cborNoTag decodeCbor serializer())

        val idFF = IdFF(0)
        val cborIdFF = "DBFFFFFFFFFFFFFFFFA1626964DBFFFFFFFFFFFFFFFF00".hex()
        assertContentEquals(cborIdFF, idFF encodeCbor serializer())
        assertEquals(idFF, cborIdFF decodeCbor serializer())
        assertEquals(idFF, cborNoTag decodeCbor serializer())
    }

    @Serializable
    @CborTag(0, true)
    data class TagOnClassRequired(val items: List<Int>)

    @Test
    fun requiredTagOnClass() {
        val intList = TagOnClassRequired(listOf(0, 0))
        val cborIntList = "C0A1656974656D73820000".hex()
        val cborIntNoTag = "A1656974656D73820000".hex()

        assertContentEquals(cborIntList, intList encodeCbor serializer())
        assertEquals(intList, cborIntList decodeCbor serializer())
        assertFailsWith<CborDecoderException> {
            assertEquals(intList, cborIntNoTag decodeCbor serializer())
        }
    }

    @Serializable
    data class TagOnFieldRequired(@CborTag(0, true) val items: List<Int>)

    @Test
    fun requiredTagOnField() {
        val intList = TagOnFieldRequired(listOf(0, 0))
        val cborIntList = "A1656974656D73C0820000".hex()
        val cborIntNoTag = "A1656974656D73820000".hex()
        assertContentEquals(cborIntList, intList encodeCbor serializer())
        assertEquals(intList, cborIntList decodeCbor serializer())
        assertFailsWith<CborDecoderException> {
            assertEquals(intList, cborIntNoTag decodeCbor serializer())
        }
    }

    @JvmInline
    @Serializable
    value class InlineFieldTag(@CborTag(0) val items: List<Int>)

    @Test
    fun inlineFieldTag() {
        val intList = InlineFieldTag(listOf(0, 0))
        val cborIntList = "C0820000".hex()

        assertContentEquals(cborIntList, intList encodeCbor serializer())
        assertEquals(intList, cborIntList decodeCbor serializer())
    }

    @JvmInline
    @Serializable
    @CborTag(0)
    value class InlineClassTag(val items: List<Int>)

    @Test
    fun inlineClassTag() {
        val intList = InlineClassTag(listOf(0, 0))
        val cborIntList = "C0820000".hex()
        assertContentEquals(cborIntList, intList encodeCbor serializer())
        assertEquals(intList, cborIntList decodeCbor serializer())
    }

    @JvmInline
    @Serializable
    @CborTag(0)
    value class InlineClassAndFieldTag(@CborTag(0) val items: List<Int>)

    @Test
    fun inlineClassAndFieldTag() {
        val intList = InlineClassAndFieldTag(listOf(0, 0))
        val cborIntList = "C0C0820000".hex()
        assertContentEquals(cborIntList, intList encodeCbor serializer())
        assertEquals(intList, cborIntList decodeCbor serializer())
    }
}