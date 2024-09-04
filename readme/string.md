# Handling MAJOR (type 3) indefinite string

Like [Handling MAJOR (type 2) bytes and indefinite bytes](bytes) an indefinite major (type 3) string is like an
array.

So you can choose to encode it with a special serializer:

- [CborListStringSerializer.kt](../src/commonMain/kotlin/net/orandja/obor/serializer/CborListStringSerializer.kt)
  To serialize a `List<String>` as an indefinite text string.
- [CborArrayStringSerializer.kt](../src/commonMain/kotlin/net/orandja/obor/serializer/CborArrayStringSerializer.kt)
  To serialize an `Array<String>` as an indefinite text string.

Example :

```kotlin
println(Cbor.encodeToHexString(CborArrayStringSerializer, arrayOf("foo", "bar")))
// 7f63666f6f63626172ff
// 7F           # text(*)
//    63        # text(3)
//       666F6F # "foo"
//    63        # text(3)
//       626172 # "bar"
//    FF        # primitive(*)

@Serializable
class Foo(@Serializable(CborListStringSerializer::class) val items: List<String>)
println(Cbor.encodeToHexString(Foo(listOf("foo", "bar"))))
// a1656974656d737f63666f6f63626172ff
// A1               # map(1)
//    65            # text(5)
//       6974656D73 # "items"
//    7F            # text(*)
//       63         # text(3)
//          666F6F  # "foo"
//       63         # text(3)
//          626172  # "bar"
//       FF         # primitive(*)
```

Or use the [CborIndefinite.kt](../src/commonMain/kotlin/net/orandja/obor/annotations/CborIndefinite.kt) annotation, it will
split the string in chunk of 255 characters and encode it with indefinite text string.

Example :

```kotlin
@Serializable
class IndefiniteString(@CborIndefinite val foo: String)
println(Cbor.encodeToHexString(IndefiniteString("foo")))
// a163666f6f7f63666f6fff
// A1              # map(1)
//    63           # text(3)
//       666F6F    # "foo"
//    7F           # text(*)
//       63        # text(3)
//          666F6F # "foo"
//       FF        # primitive(*)
```