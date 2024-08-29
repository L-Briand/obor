package net.orandja.obor

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTuple
import kotlin.test.Test

class CborTupleTest {
    @Serializable
    @CborTuple
    data class Foo(val bar: Int, val baz: String)

    @Test
    fun encodeAsTuple() {
        assertTransformation("82006161".hex(), Foo(0, "a"))
    }
}