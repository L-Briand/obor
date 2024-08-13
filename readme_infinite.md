# Inifinite mark on serialization

CBOR major type 2, 3, 4 and 5 can be written as *infinite* by merging the major tag with `0x1F`.

- Major (type 2) infinite byte is described [here](readme_bytes.md)
- Major (type 3) infinite string is described [here](readme_string.md)

For Major types 4 and 5, you can use
the [CborInfinite.kt](src/commonMain/kotlin/net/orandja/obor/annotations/CborInfinite.kt) annotation to indicate the
serializer to encode the list or an object with the infinite marker.

Example:

```kotlin
@CborInfinite
@Serializable
data class Foo(@CborInfinite val bar: List<Int>)
println(Cbor.encodeToHexString(Foo(listOf(1, 2, 3))))
// bf636261729f010203ffff
// BF           # map(*)
//    63        # text(3)
//       626172 # "bar"
//    9F        # array(*)
//       01     # unsigned(1)
//       02     # unsigned(2)
//       03     # unsigned(3)
//       FF     # primitive(*)
//    FF        # primitive(*)
```

