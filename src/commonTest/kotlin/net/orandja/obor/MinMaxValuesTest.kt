package net.orandja.obor

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.orandja.obor.codec.Cbor
import kotlin.test.Test
import kotlin.test.assertEquals

class MinMaxValuesTest {
    private inline fun <reified T> doubleTransformations(value: T, serializer: KSerializer<T> = serializer<T>()): T {
        return Cbor.decodeFromByteArray(serializer, Cbor.encodeToByteArray(serializer, value))
    }

    @Test
    fun byteMinMax() {
        assertEquals(Byte.MIN_VALUE, doubleTransformations(Byte.MIN_VALUE))
        assertEquals(Byte.MAX_VALUE, doubleTransformations(Byte.MAX_VALUE))
    }

    @Test
    fun shortMinMax() {
        assertEquals(Short.MIN_VALUE, doubleTransformations(Short.MIN_VALUE))
        assertEquals(Short.MAX_VALUE, doubleTransformations(Short.MAX_VALUE))
    }

    @Test
    fun intMinMax() {
        assertEquals(Int.MIN_VALUE, doubleTransformations(Int.MIN_VALUE))
        assertEquals(Int.MAX_VALUE, doubleTransformations(Int.MAX_VALUE))
    }

    @Test
    fun longMinMax() {
        assertEquals(Long.MIN_VALUE, doubleTransformations(Long.MIN_VALUE))
        assertEquals(Long.MAX_VALUE, doubleTransformations(Long.MAX_VALUE))
    }
}