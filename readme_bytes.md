# Handling MAJOR (type 2) bytes and infinite bytes

Before handling major byte `0x40` you need to understand a few things.

- Kotlin treat `ByteArray`, `Array<Byte>`, `List<Byte>` like a list of many Byte elements. The default behavior is to
  encode them with major array `0x80`. So if you were to encode `byteArrayOf(1, 2, 3)` it will result in `0x83010203`
  not in `0x43010203`. Also, it will encode it byte by byte, which can drastically decrease performance.
- Conceptually, an infinite major byte string like `0x5F41004101FF` is like an infinite array with a start identifier
  `5F` elements inside `4100` and a closing tag `FF` only the elements are restrained to be major (type 2) bytes. 

With that in mind, there are two ways to encode with major (type 2).

1. Use a custom serializers:
    - [CborByteArraySerializer.kt](src/commonMain/kotlin/net/orandja/obor/serializer/CborByteArraySerializer.kt).
      To serialize a `ByteArray` in one go.
    - [CborListByteArraySerializer.kt](src/commonMain/kotlin/net/orandja/obor/serializer/CborListByteArraySerializer.kt)
      To serialize a `List<ByteArray>` as an infinite byte string.
    - [CborArrayByteArraySerializer.kt](src/commonMain/kotlin/net/orandja/obor/serializer/CborArrayByteArraySerializer.kt)
      To serialize an `Array<ByteArray>` as an infinite byte string.

You can use the value class [CborBytes.kt](src/commonMain/kotlin/net/orandja/obor/CborBytes.kt) to always write
ByteArray values with CborByteArraySerializer.

Example:

```kotlin
println(Cbor.encodeToHexString(CborByteArraySerializer, byteArrayOf(1, 2, 3)))
// 43010203
// 43     # bytes(3)
// 010203 # "\u0001\u0002\u0003"

println(Cbor.encodeToHexString(CborListByteArraySerializer, listOf(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6))))
// 5f4301020343040506ff
// 5F           # bytes(*)
//    43        # bytes(3)
//       010203 # "\u0001\u0002\u0003"
//    43        # bytes(3)
//       040506 # "\u0004\u0005\u0006"
//    FF        # primitive(*)

@Serializable
class Foo(@Serializable(CborByteArraySerializer::class) val bytes: ByteArray)
println(Cbor.encodeToHexString(Foo(byteArrayOf(1, 2, 3))))
// a165627974657343010203
// A1               # map(1)
//    65            # text(5)
//       6279746573 # "bytes"
//    43            # bytes(3)
//       010203     # "\u0001\u0002\u0003"
```

2. Use annotation `@CborRawBytes` and `@CborInfinite`
    - `@CborRawBytes` can be applied on any type that looks like a list of bytes (`ByteArray`, `List<Byte`,
      `Array<Byte>`) and encode it with major (type 2) byte instead of major (type 4) array. **It will still write each
      element one by one**.
    - `@CborInfinite` if applied on top of `@CborRawByte` it will encode the bytes in chunk of 255. 255 is arbitrary and
      hard coded inside the serializer.

Examples:

```kotlin
@Serializable
class Bytes(@CborRawBytes val bytes: ByteArray)
println(Cbor.encodeToHexString(Bytes(byteArrayOf(1, 2, 3))))
// a165627974657343010203
// A1               # map(1)
//    65            # text(5)
//       6279746573 # "bytes"
//    43            # bytes(3)
//       010203     # "\u0001\u0002\u0003"

@Serializable
class ListBytes(@CborInfinite @CborRawBytes val bytes: List<Byte>)
println(Cbor.encodeToHexString(ListBytes(listOf<Byte>(1, 2, 3))))
// a16562797465735f43010203ff
// A1               # map(1)
//    65            # text(5)
//       6279746573 # "bytes"
//    5F            # bytes(*)
//       43         # bytes(3)
//          010203  # "\u0001\u0002\u0003"
//       FF         # primitive(*)
```

