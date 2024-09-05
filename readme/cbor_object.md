# Handle any data structure

Like the `JsonObject`, you can use the [CborObject](../src/commonMain/kotlin/net/orandja/obor/data/CborObject.kt) to
encode / decode anything. Even structures which are challenging to create with the kotlin language.

This object can be great, maybe a bit cumbersome, for polymorphic serialization.

The `toString` method neatly prints the content of the cbor object.

# Encoding / Decoding

You can decode any cbor data to `CborObject`:

```kotlin
@Serializable
class Foo(val foo: Float, val bar: String)

val data = Cbor.encodeToByteArray(Foo(8.0f, "hello"))
// data == A263666F6FFA41000000636261726568656C6C6F
val cborObject = Cbor.decodeFromByteArray<CborObject>(data)
assertContentEquals(data, Cbor.encodeToByteArray(cborObject))
assertContentEquals(data, cborObject.cbor)

println(cborObject.cborAsHexString)
println(cborObject.toString())
```

It prints:

```
A263666F6FFA41000000636261726568656C6C6F
A2               # map(2)
   63            # text(3)
      666F6F     # "foo"
   FA41000000    # float32(8.0)
   63            # text(3)
      626172     # "bar"
   65            # text(5)
      68656C6C6F # "hello"
```

## Subclasses

There are a total of 14 different subclasses
of [CborObject.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborObject.kt)

The ones corresponding to simple types:

- [CborPositive.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborPositive.kt)
- [CborNegative.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborNegative.kt)
- [CborBoolean.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborBoolean.kt)
- [CborFloat.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborFloat.kt)
- [CborNull.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborNull.kt)
- [CborUndefined.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborUndefined.kt)
- [CborBytes.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborBytes.kt)
- [CborText.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborText.kt)

Special cases for indefinite length bytes/text:

- [CborBytesIndefinite.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborBytesIndefinite.kt)
- [CborTextIndefinite.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborTextIndefinite.kt)

Array and maps:

- [CborArray.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborArray.kt)
- [CborMap.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborMap.kt)
- [CborMapEntry.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborMapEntry.kt)

**NOTE**: Since CBOR RFC allows a map to have multiple keys of the same element **CborMap** is a like a
`List<CborMapEntry>`and not a `Map<CborObject, CborObject>`. So when you use it as a map, you should use the
`CborMap.asMap` method.

Tagged element:

- [CborTagged.kt](../src/commonMain/kotlin/net/orandja/obor/data/CborTagged.kt)

## Constructing CborObject

You can use the many functions from `CborObject` or the DSL like builder from `buildArray`, `buildMap`,
`buildOrderedMap`.

An example is better than words.

```kotlin
val obj = CborObject.buildMap {
    put(value("foo"), value(0))
    put(value("bar"), buildArray {
        add(tag(1234, value("hello")))
    })
}
assertEquals("a263666f6f006362617281d904d26568656c6c6f", Cbor.encodeToHexString(obj))
println(obj.toString())
```

```
A2                     # map(2)
   63                  # text(3)
      666F6F           # "foo"
   00                  # unsigned(0)
   63                  # text(3)
      626172           # "bar"
   81                  # array(1)
      D904D2           # tag(1234)
         65            # text(5)
            68656C6C6F # "hello"
```

From the last example, trying to decode the object might look like this.

```kotlin
val obj = Cbor.decodeFromHexString<CborObject>("a263666f6f006362617281d904d26568656c6c6f")
assertTrue(obj is CborMap)
val foo = obj.asMap[CborObject.value("foo")]
assertNotNull(foo)
assertEquals(CborObject.positive(0), foo)
val bar = obj.asMap[CborObject.value("bar")]
assertNotNull(bar)
assertTrue(bar is CborArray)
assertEquals(1, bar.size)
val tag = bar[0]
assertTrue(tag is CborTagged)
assertEquals(1234, tag.tag)
assertEquals(CborObject.value("hello"), tag.value)
```

Methods:

```
CborObject.value(Byte, Short, Int, Long)  : CborPositive or CborNegative
CborObject.value(Boolean)                 : CborBoolean 
CborObject.value(Float, Double)           : CborFloat
CborObject.value(String)                  : CborText
CborObject.value(ByteArray)               : CborBytes

CborObject.nullElement : CborNull
CborObject.undefined   : CborUndefined

CborObject.tag(tag: Long, CborObject) : CborTagged

CborObject.positive(UByte, UShort, UInt, ULong) : CborPositive
CborObject.negative(UByte, UShort, UInt, ULong) : CborNegative

CborObject.indefiniteText(List<String>)     : CborTextIndefinite
CborObject.indefiniteBytes(List<ByteArray>) : CborBytesIndefinite

CborObject.indefiniteTextBuilder { this == MutableList<String> }     : CborTextIndefinite 
CborObject.indefiniteBytesBuilder { this == MutableList<ByteArray> } : CborBytesIndefinite

CborObject.array(List<CborObject>, indefinite: Boolean)          : CborArray 
CborObject.map(Map<CborObject, CborObject>, indefinite: Boolean) : CborMap
CborObject.orderedMap(List<CborMapEntry>, indefinite: Boolean)   : CborMap

CborObject.buildArray { this == MutableList<CborObject> }          :CborArray 
CborObject.buildMap { this == MutableMap<CborObject, CborObject> } :CborMap 
CborObject.buildOrderedMap { this == MutableList<CborMapEntry> }   :CborMap 
```
