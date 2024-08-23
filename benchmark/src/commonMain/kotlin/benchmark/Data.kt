package benchmark

import kotlinx.serialization.Serializable
import net.orandja.obor.codec.Cbor

@Serializable
data class Foo(val foo: String, val bar: Bar, val baz: Boolean)

@Serializable
data class Bar(val bar: Int, val baz: List<Int>)

val FOO_DATA = Foo("Hello", Bar(3, listOf(1, 2, 3)), false)
val FOO_CBOR = Cbor.encodeToByteArray(Foo.serializer(), FOO_DATA)