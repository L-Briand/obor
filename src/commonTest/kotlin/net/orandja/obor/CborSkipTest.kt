package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import net.orandja.obor.annotations.CborSkip
import net.orandja.obor.codec.Cbor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CborSkipTest {
    companion object {
        private val FULL_HEX = "A36161016162FA3F80000061638101"
        private val FULL = FULL_HEX.hex()
    }

    @Serializable
    data class A(@CborSkip val a: Int? = null, val b: Float? = null, val c: List<Int>? = null)

    @Serializable
    data class B(val a: Int? = null, @CborSkip val b: Float? = null, val c: List<Int>? = null)

    @Serializable
    data class C(val a: Int? = null, val b: Float? = null, @CborSkip val c: List<Int>? = null)

    @Serializable
    data class D(val a: A? = null, @CborSkip val b: B? = null, val c: C? = null)

    @Test
    fun testCborSkipA() {
        assertTransformation("A26162F66163F6".hex(), A())
        val encoded = Cbor.encodeToByteArray(A(1, 1f, listOf(1)))
        assertContentEquals("A26162FA3F80000061638101".hex(), encoded)
        run {
            val decoded = Cbor.decodeFromByteArray<A>(encoded)
            assertEquals(A(b = 1f, c = listOf(1)), decoded)
        }
        run {
            val decoded = Cbor.decodeFromByteArray<A>(FULL)
            assertEquals(A(b = 1f, c = listOf(1)), decoded)
        }
    }

    @Test
    fun testCborSkipB() {
        assertTransformation("A26161F66163F6".hex(), B())
        val encoded = Cbor.encodeToByteArray(B(1, 1f, listOf(1)))
        assertContentEquals("A261610161638101".hex(), encoded)
        run {
            val decoded = Cbor.decodeFromByteArray<B>(encoded)
            assertEquals(B(a = 1, c = listOf(1)), decoded)
        }
        run {
            val decoded = Cbor.decodeFromByteArray<B>(FULL)
            assertEquals(B(a = 1, c = listOf(1)), decoded)
        }
    }

    @Test
    fun testCborSkipC() {
        assertTransformation("A26161F66162F6".hex(), C())
        val encoded = Cbor.encodeToByteArray(C(1, 1f, listOf(1)))
        assertContentEquals("A26161016162FA3F800000".hex(), encoded)
        run {
            val decoded = Cbor.decodeFromByteArray<C>(encoded)
            assertEquals(C(a = 1, b = 1f), decoded)
        }
        run {
            val decoded = Cbor.decodeFromByteArray<C>(FULL)
            assertEquals(C(a = 1, b = 1f), decoded)
        }
    }

    @Test
    fun testCborSkipD() {
        assertTransformation("A26161F66163F6".hex(), D())
        val encoded = Cbor.encodeToByteArray(D(A(1, 1f, listOf(1)), B(1, 1f, listOf(1)), C(1, 1f, listOf(1))))
        assertContentEquals("A26161A26162FA3F800000616381016163A26161016162FA3F800000".hex(), encoded)
        run {
            val decoded = Cbor.decodeFromByteArray<D>(encoded)
            assertEquals(D(A(null, 1f, listOf(1)), null, C(1, 1f, null)), decoded)
        }
        run {
            val decoded = Cbor.decodeFromByteArray<D>("A36161${FULL_HEX}6162${FULL_HEX}6163${FULL_HEX}".hex())
            assertEquals(D(A(null, 1f, listOf(1)), null, C(1, 1f, null)), decoded)
        }
    }
}