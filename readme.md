# OBOR

An implementation of the serial format [CBOR](https://cbor.io/) with Kotlin multiplatform and jetbrains
kotlinx-serialization api.

# Usage

You can find obor library on maven central.

For Jvm:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:x.y.z")
    implementation("net.orandja.obor:obor:2.1.0")
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
                implementation("net.orandja.obor:obor:2.1.0")
            }
        }
    }
}
```

# Example

It uses conventionnal encode/decode kotlinx-serialization functions like JSON. You can take a look on how it
works [here](https://github.com/Kotlin/kotlinx.serialization#introduction-and-references).

```kotlin
import net.orandja.obor.codec.Cbor
import kotlinx.serialization.Serializable

@Serializable
data class Example(val foo: String, val bar: Int)

fun main() {
    val example = Example("Hello World !", 42)
    val encode = Cbor.encodeToHexString(example)
    assert(encode == "a263666f6f6d48656c6c6f20576f726c64202163626172182a")
    val decode = Cbor.decodeFromHexString<Example>(encode)
    assert(example == decode)
}
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

The Cbor codec can be configured with :

```kotlin
val codec = Cbor {
    ingnoreUnknownKeys = false // Set to true to ignore unknown keys during deserialization  
}
```

## Features

- It always uses the least number of bytes to encode something.
  An Int with a value of less than 24 will be encoded to a single byte.
  A float32 that can be encoded with a float16 will be encoded as a float16.


- Like the `JsonObject`, you can use the [CborObject](src/commonMain/kotlin/net/orandja/obor/data/CborObject.kt) to
  encode / decode anything. Even structures which are challenging to create with the kotlin language.
  See [Handle any data structure](readme/cbor_object.md)


- By implementing the [Reader](src/commonMain/kotlin/net/orandja/obor/io/Reader.kt)
  or [Writer](src/commonMain/kotlin/net/orandja/obor/io/Writer.kt) interface, you are able to read and write CBOR from
  anything. Know how many bytes were read/written. See [Reading and writing to anything](readme/io.md)


- There are special
  serializers (in [CborUnsignedSerializer](src/commonMain/kotlin/net/orandja/obor/serializer/CborUnsignedSerializer.kt))
  to serialize and deserialize full range of signed and unsigned values. See [Unsigned values](readme/unsigned.md)


- Create any tagged CBOR data with the `@CborTag` annotation. See [Creating tagged elements](readme/tags.md)


- ByteArray and types looking like `List<Byte>` can be written with Major type 2 instead of Major type 4 with
  `@CborRawBytes` and special serializers. See [Handling MAJOR (type 2) bytes and indefinite bytes](readme/bytes.md)


- Indefinite length is handled automatically on deserialization and can be specified on serialization with
  `@CborIndefinite` or special serializers.


- You can compact the serialization of a class to its ordered properties with `@CborTuple`. </br>
  `{ a: "foo", b: 0 }` -> `[ "foo", 0 ]`


- You can shrink down the serialization further if a `@CborTuple` is in a list with `@CborTuple.inlineInList`. </br>
  `[{ a: "foo", b: 0 }, { a: "bar", b: 1 }]` -> `[ "foo", 0, "bar", 1]`

## Annotations

- **[CborTuple](src/commonMain/kotlin/net/orandja/obor/annotations/CborTuple.kt)** A class or object annotated with it
  will be serialized as an ordered list of its elements.

- **[CborIndefinite](src/commonMain/kotlin/net/orandja/obor/annotations/CborIndefinite.kt)**
  Indicates to the codec to always use indefinite length encoding when encoding the annotated value. For example:
    - `class Foo(val a: List<Int>)` -> `Foo(listOf(0,1))` will be serialized to `A16161820001`
    - `@CborIndefinite class Foo(val a: List<Int>)` -> `Foo(listOf(0,1))` will be serialized to `BF6161820001FF`

- **[CborRawBytes](src/commonMain/kotlin/net/orandja/obor/annotations/CborRawBytes.kt)**
  When a type looks like a `List<Byte>` (`Array<Byte>`, `ByteArray`) and is annotated with `@CborRawBytes` It will use
  the
  major type 2 (byte string) instead of major type 4 (array).
    - `class Foo(val a: List<Byte>)` -> `Foo(listOf(0, 1))` will be serialized to `A16161820001`
    - `class Foo(@CborRawBytes val a: List<Byte>)` -> `Foo(listOf(0, 1))` will be serialized to `A16161420001`

- **[CborSkip](src/commonMain/kotlin/net/orandja/obor/annotations/CborSkip.kt)** Annotates a field to skip it during
  serialization and deserialization

- **[CborTag](src/commonMain/kotlin/net/orandja/obor/annotations/CborTag.kt)** Adds a tag to the specified field or
  class.

## Notice

The use of `@CborIndefinite` only affects serialization and not deserialization. Unless you use the specific special
serializers as described in [Handling MAJOR (type 2) bytes and indefinite bytes](readme/bytes.md)
or [Handling MAJOR (type 3) indefinite string](readme/string.md).

An indefinite marker will be deserialized like a non-indefinite one. For example `0x9f010203ff` and `0x83010203` can
both be
deserialized to `listOf(1, 2, 3)`

```kotlin
assertEquals(
    Cbor.decodeFromHexString<List<Int>>("83010203"),
    Cbor.decodeFromHexString<List<Int>>("9f010203ff")
)
```

## Further read

- [Handle any data structure](readme/cbor_object.md)
- [Reading and writing to anything](readme/io.md)
- [Unsigned values](readme/unsigned.md)
- [Handling MAJOR (type 2) bytes and indefinite bytes](readme/bytes.md). Speed up serializing/deserializing
- [Handling MAJOR (type 3) indefinite string](readme/string.md)
- [Indefinite mark on serialization](readme/indefinite.md)
- [Creating tagged elements](readme/tags.md)
