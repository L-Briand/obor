# OBOR

An implementation of the serial format [CBOR](https://cbor.io/) with Kotlin multiplatform and jetbrains
kotlinx-serialization api.

# Usage

You can find obor library on maven central.

For Jvm:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:x.y.z")
    implementation("net.orandja.obor:obor:2.0.0")
}
```

For multiplatform:

```kotlin
kotlin {
    ...
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:x.y.z")
                implementation("net.orandja.obor:obor:2.0.0")
            }
        }
    }
}
```

# Example

It uses conventionnal encode/decode kotlinx-serialization functions like JSON. You can take a look on how it
works [here](https://github.com/Kotlin/kotlinx.serialization#introduction-and-references).

```kotlin
@Serializable
data class Example(val foo: String, val bar: Int)

fun main() {
    val example = Example("Hello World !", 42)
    // Encode example into ByteArray
    val encode = Cbor.encodeToHexString(example)
    println(encode.joinToString("", "0x") { "%02X".format(it) })
    // Decode example from ByteArray
    val decode = Cbor.decodeFromHexString(Example.serializer(), encode)
    println(decode == example)
}
```

This prints:

```
a263666f6f6d48656c6c6f20576f726c64202163626172182a
true
```

The bytes translate to:

```
A2                               # map(2)
   63                            # text(3)
      666F6F                     # "foo"
   6D                            # text(13)
      48656C6C6F20576F726C642021 # "Hello World !"
   63                            # text(3)
      626172                     # "bar"
   18 2A                         # unsigned(42)
```

## Notice

The use of `@CborInfinite` only affects serialization and not deserialization. Unless you use the specific special
serializers as described in [Handling MAJOR (type 2) bytes and infinite bytes](readme_bytes.md)
or [Handling MAJOR (type 3) infinite string](readme_string.md).

An infinite marker will be deserialized like a non-infinite one. For example `0x9f010203ff` and `0x83010203` can both be
deserialized to `listOf(1, 2, 3)`

```kotlin
assertEquals(
    Cbor.decodeFromHexString<List<Int>>("83010203"),
    Cbor.decodeFromHexString<List<Int>>("9f010203ff")
)
```

## Annotations

- [CborInfinite](src/commonMain/kotlin/net/orandja/obor/annotations/CborInfinite.kt)
- [CborRawBytes](src/commonMain/kotlin/net/orandja/obor/annotations/CborRawBytes.kt)
- [CborSkip](src/commonMain/kotlin/net/orandja/obor/annotations/CborSkip.kt)
- [CborTag.kt](src/commonMain/kotlin/net/orandja/obor/annotations/CborTag.kt)

## Further read

- [Unsigned values](readme_unsigned.md)
- [Handling MAJOR (type 2) bytes and infinite bytes](readme_bytes.md)
- [Handling MAJOR (type 3) infinite string](readme_string.md)
- [Inifinite mark on serialization](readme_infinite.md)
- [Creating tagged elements](readme_tags.md)
