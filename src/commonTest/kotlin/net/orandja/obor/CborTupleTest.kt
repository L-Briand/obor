package net.orandja.obor

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTuple
import kotlin.test.Test

class CborTupleTest {
    @Serializable
    @CborTuple
    data class Foo(val foo: Int, val bar: String)

    @Serializable
    @CborTuple(inlinedInList = true)
    data class Bar(val foo: Int, val bar: String)

    @Test
    fun singleTuple() {
        assertTransformation("82006161".hex(), Foo(0, "a"))
        assertTransformation("82006161".hex(), Bar(0, "a"))
    }

    @Test
    fun listOfTuple() {
        assertTransformation("828200616182016162".hex(), listOf(Foo(0, "a"), Foo(1, "b")))
        assertTransformation("84006161016162".hex(), listOf(Bar(0, "a"), Bar(1, "b")))
    }
}