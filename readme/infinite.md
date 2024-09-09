# Indefinite mark on serialization

CBOR major type 2, 3, 4 and 5 can be written as *indefinite* by merging the major tag with `0x1F`.

- Major (type 2) indefinite byte is described [here](bytes)
- Major (type 3) indefinite string is described [here](string)

For Major types 4 and 5, you can use
the [CborIndefinite.kt](../src/commonMain/kotlin/net/orandja/obor/annotations/CborIndefinite.kt) annotation to indicate
the
serializer to encode the list or an object with the indefinite marker.

Example:

```kotlin
@CborIndefinite
@Serializable
data class Foo(@CborIndefinite val bar: List<Int>)
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

