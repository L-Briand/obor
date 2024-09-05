package net.orandja.obor

import kotlinx.serialization.*
import net.orandja.obor.annotations.CborIndefinite
import net.orandja.obor.annotations.CborTuple
import net.orandja.obor.codec.Cbor
import net.orandja.obor.data.CborArray
import net.orandja.obor.data.CborMap
import net.orandja.obor.data.CborObject
import net.orandja.obor.data.CborTagged
import kotlin.test.*

class CborObjectTest {

    @Test
    fun basicObject() {
        @Serializable
        @CborTuple
        data class Foo(val a: Int, val b: Int, val c: String, @CborIndefinite val d: String, val e: Float)

        val rootCbor = Cbor.encodeToByteArray(Foo(1, -1, "a", "b", 1f))
        val cborObject = Cbor.decodeFromByteArray<CborObject>(rootCbor)
        val sameAsRoot = Cbor.encodeToByteArray(cborObject)
        assertContentEquals(rootCbor, sameAsRoot)
    }

    @Test
    fun readmeExample() {
        @Serializable
        data class Foo(val foo: Float, val bar: String)

        val foo = Foo(8.0f, "hello")
        val data = Cbor.encodeToByteArray(foo) // A263666F6FFA41000000636261726568656C6C6F
        val cborObject = Cbor.decodeFromByteArray<CborObject>(data)
        assertContentEquals(data, Cbor.encodeToByteArray(cborObject))
        assertEquals(foo, Cbor.decodeFromCborObject(Foo.serializer(), cborObject))
    }

    @Test
    fun createObject() {
        val obj = CborObject.buildMap {
            put(value("foo"), value(0))
            put(value("bar"), buildArray {
                add(tag(1234, value("hello")))
            })
        }
        assertEquals("a263666f6f006362617281d904d26568656c6c6f", Cbor.encodeToHexString(obj))
        println(obj.toString())
    }

    @Test
    fun decodeObject() {
        val obj = Cbor.decodeFromHexString<CborObject>("a263666f6f006362617281d904d26568656c6c6f")
        assertTrue(obj is CborMap)
        val foo = obj.asMap[CborObject.value("foo")]
        assertNotNull(foo)
        assertEquals(CborObject.positive(0), foo)
        val bar = obj.asMap[CborObject.value("bar")]
        assertNotNull(bar)
        assertTrue(bar is CborArray)
        assertEquals(1, bar.size)
        val tag = bar[0]
        assertTrue(tag is CborTagged)
        assertEquals(1234, tag.tag)
        assertEquals(CborObject.value("hello"), tag.value)
    }

    @Test
    fun everyCborObject() {
        val obj = CborObject.buildMap {
            this[value("key")] = buildArray {
                add(value(1))
                add(value(-1))
                add(value(1f))
                add(value(true))
                add(value("a"))
                add(value(byteArrayOf(0x01)))
                add(nullElement)
                add(undefined)
                add(tag(1234, value(1)))
                add(indefiniteText("a"))
                add(indefiniteBytes(byteArrayOf(0x01)))
            }
        }
        assertTransformation("A1636B65798B0120FA3F800000F561614101F6F7D904D2017F6161FF5F4101FF".hex(), obj)
        assertContentEquals("A1636B65798B0120FA3F800000F561614101F6F7D904D2017F6161FF5F4101FF".hex(), obj.cbor)
        assertEquals("A1636B65798B0120FA3F800000F561614101F6F7D904D2017F6161FF5F4101FF", obj.cborAsHexString)
    }
}