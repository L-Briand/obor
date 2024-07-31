package net.orandja.obor

import kotlin.test.Test

class BooleanTest {

    @Test
    fun boolean() {
        assertTransformation("F4".hex(), false)
        assertTransformation("F5".hex(), true)
    }
}