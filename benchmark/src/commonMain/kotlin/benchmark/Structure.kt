package benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import net.orandja.obor.codec.Cbor as Obor

@OptIn(ExperimentalSerializationApi::class)
@State(Scope.Benchmark)
class Structure {

    @Benchmark
    fun encodeCbor() {
        Cbor.encodeToByteArray(Foo.serializer(), FOO_DATA)
    }

    @Benchmark
    fun decodeCbor() {
        Cbor.decodeFromByteArray(Foo.serializer(), FOO_CBOR)
    }

    @Benchmark
    fun encodeObor() {
        Obor.encodeToByteArray(Foo.serializer(), FOO_DATA)
    }

    @Benchmark
    fun decodeObor() {
        Obor.decodeFromByteArray(Foo.serializer(), FOO_CBOR)
    }
}