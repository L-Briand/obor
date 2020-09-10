# OBOR

A JVM CBOR serializer/deserializer implementation with kotlinx-serialization.
(I believe it can be ported to JavaScript or Kotlin Native with little efforts since the majority of the code don't use JVM SDK)

## Usage

It uses conventionnal encode/decode kotlinx-serialization functions like JSON.
You can take a look on how it works [here](https://github.com/Kotlin/kotlinx.serialization#introduction-and-references).

Example :
```kotlin
@Serializable
data class Example(val foo: String, val bar: Int)

fun main() {
    val example = Example("Hello World !", 42)
    // Encode example into ByteArray
    val encode = Cbor.encodeToByteArray(Example.serializer(), example)
    println(encode.joinToString("", "0x") { "%02X".format(it) })
    // Decode example from ByteArray
    val decode = Cbor.decodeFromByteArray(Example.serializer(), encode)
    println(decode == example)
}
```

This prints :
```
0xA263666F6F6D48656C6C6F20576F726C64202163626172182A
true
```

The bytes translate to : 
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

## Annotations

### `CborRawBytes`

Annotate a `ByteArray`, `Array<Byte>` or `List<Byte>` to encode it as [major type 2 byte string](https://tools.ietf.org/html/rfc7049#section-2.1).
It is not necessary to annotate a field with `@CborRawBytes` to decode Major 2. 

```kotlin
@Serializable
class Data(
    @CborRawBytes
    val array: ByteArray
)
```

### `CborInfinite(chunkSize: Int)`

`chunkSize` define the number of element before the structure become infinite. 
`chunkSize` of `0` means its always infinite. A negative value is the same as the default implementation: always try to write the size.  
* If applied to a class or an object: `chunkSize` correspond the number of fields before the structure is serialize with infinite Mark.
* If applied on a Map or a List: `chunkSize` correspond to the number of elements before the list is serialize with infinite Mark. 
* If applied on a String or a ByteArray with `@CborRawBytes`: `chunksize` correspond to the length in which the element is chunk.

```kotlin
@Serializable
@CborInfinite(0) // Data will be encoded with [Major 5 (MAP) infinite]
// in contrast @CborInfinite(2) will encode [Major 5 (MAP) size] since there are only 2 elements 
class Data(
    @CborInfinite(10) // if list.size > 10 then [Major 4 (LIST) infinite] else [Major 4 (LIST) size] 
    val array: List<String>,
    @CborInfinite(10) // if string.length > 10 then [Major 3 (TEXT) infinite] else [Major 3 (TEXT) size]
    val string: String
)
```

## Skipped fields

If a field key isn't present in the class or object declaration but is inside a CBOR message it is ommited by the decoder.

```kotlin
@Serializable
data class Data1(val s: String, val i: Int)
@Serializable
data class Data2(val i: Int)

val data1 = Data1("", 42)
val twoFields = Cbor.encodeToByteArray(Data1.serializer(), data1)
val data2 = Cbor.decodeFromByteArray(Data2.serializer(), twoFields)

assert(data2.i == data1.i)
``` 

## Iterable serializer

You can encode any `Iterable<T>` as an infinite Cbor Array (Major 5) with the `IterableSerializer`

```kotlin
@Serializable
class Data(
    @Serializable(IterableSerializer::class)
    val iterable : Iterable<String>
)

fun main() {
    val encoded = Cbor.encodeToByteArray(Data.serializer(), Data(listOf("Hello", "World")))
    println(encoded.joinToString("") { "%02X".format(it) })
}
```

Output : 
```
A1                     # map(1)
   68                  # text(8)
      6974657261626C65 # "iterable"
   9F                  # array(*)
      65               # text(5)
         48656C6C6F    # "Hello"
      65               # text(5)
         576F726C64    # "World"
      FF               # primitive(*)
``` 

## Streams

Alongside `encodeToByteArray` and `decodeFromByteArray` you can use `decodeFromInputStream` and`encodeToOutputStream` the same way.
 
```kotlin

@Serializable
data class Example(val foo: String, val bar: Int)
val cbor = Cbor()
fun main() {
    val example = Example("Hello World !", 42)
    val output = ByteArrayOutputStream()
    // Encode example into OutputStream
    val encode = cbor.encodeToOutputStream(Example.serializer(), example, output)
    println(output.toByteArray().joinToString("", "0x") { "%02X".format(it) })

    val input = ByteArrayInputStream(output.toByteArray())
    // Decode example from InputStream
    val decode = cbor.decodeFromInputStream(Example.serializer(), input)
    println(decode == example)
}
```

# Todos

## Misc

- Kotlin documentation.
- Publication on maven.
- Deserialization exceptions.
- `@Serializer` for Unsigned types.
- Documentation on multiple files.

## Annotations

### `Float16`

Annotate a Float to transform it into a IEEE 754 Half-Precision Float. 

### `CborTag(tagNumber: Long, require: Boolean)`
 
A class with this annotation will be serialized with the corresponding [CBOR Tag](https://tools.ietf.org/html/rfc7049#section-2.4).
If require == true then the tag is require during deserialization.

### `CborRequire`

A field who can't be skip when decoded.

## Tests

- Major Byte (3)
- Major Text (4)
- Major Array (5)
- Major Map (6)
- Major Tag (7)
- Primitives
- Skip values
- Annotations