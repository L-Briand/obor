package net.orandja.obor

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToHexString
import net.orandja.obor.codec.Cbor
import kotlin.test.Test

class CustomTest {

    @Serializable
    data class User(val id: Int, val name: String, val amount: Double)

    @Test
    fun test() {
        println(Cbor.encodeToHexString(User.serializer(), User(1, "John", 1.0)))
    }
}