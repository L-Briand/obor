package net.orandja.obor

import kotlinx.serialization.serializer
import net.orandja.obor.Resource.FALSE
import net.orandja.obor.Resource.TRUE
import kotlin.test.Test

class BooleanTest {
    @Test
    fun boolean() {
        assertTransformation(TRUE, serializer<Boolean>(), true)
        assertTransformation(FALSE, serializer<Boolean>(), false)
    }
}