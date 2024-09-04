package net.orandja.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer
import net.orandja.obor.data.CborObject
import net.orandja.obor.codec.Cbor as Obor

@OptIn(ExperimentalSerializationApi::class)
@State(Scope.Benchmark)
class Structure {

    @Serializable
    data class Foo(val foo: String, val bar: Bar, val baz: Boolean)

    @Serializable
    data class Bar(val bar: Int, val baz: List<Int>)

    companion object {
        val foo = Foo("Hello", Bar(3, listOf(1, 2, 3)), false)
        val fooCbor = Obor.encodeToByteArray(Foo.serializer(), foo)
        val fooObject = Obor.decodeFromByteArray(serializer<CborObject>(), fooCbor)
    }

    @Benchmark
    fun encodeCbor() {
        Cbor.encodeToByteArray(Foo.serializer(), foo)
    }

    @Benchmark
    fun encodeObor() {
        Obor.encodeToByteArray(Foo.serializer(), foo)
    }

    @Benchmark
    fun encodeOborObject() {
        Obor.encodeToByteArray(serializer<CborObject>(), fooObject)
    }

    @Benchmark
    fun decodeCbor() {
        Cbor.decodeFromByteArray(Foo.serializer(), fooCbor)
    }

    @Benchmark
    fun decodeObor() {
        Obor.decodeFromByteArray(Foo.serializer(), fooCbor)
    }

    @Benchmark
    fun decodeOborObject() {
        Obor.decodeFromByteArray(serializer<CborObject>(), fooCbor)
    }
}