package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.CborDecoderException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnknownKeyTest {

    @Serializable
    data class Foo1(val foo: String, val bar: Int)

    @Serializable
    data class Foo2(val foo: String, val baz: Float, val bar: Int)

    @Test
    fun ignoreUnknownKey() {
        val codec = Cbor { this.ingnoreUnknownKeys = true }
        val data = codec.encodeToHexString(Foo2("a", 1.0f, 1))
        assertEquals(Foo1("a", 1), codec.decodeFromHexString(Foo1.serializer(), data))
    }

    @Test
    fun notIgnoreUnknownKey() {
        val codec = Cbor { this.ingnoreUnknownKeys = false }
        val data = codec.encodeToHexString(Foo2("a", 1.0f, 1))
        assertFailsWith<CborDecoderException.ClassUnknownKey> {
            codec.decodeFromHexString(Foo1.serializer(), data)
        }
    }
}