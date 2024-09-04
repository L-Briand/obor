# Creating tagged elements

A class or a property tagged with [CborTag](../src/commonMain/kotlin/net/orandja/obor/annotations/CborTag.kt) will be
prepended with the specified tag.

If the `required = true`, it means that the tag is required during deserialization. The tag will always be written
during serialization.

```kotlin
@CborTag(tag = 0x44L, required = true)
@Serializable
data class Foo(@CborTag(tag = 0x55) val bar: Int)
println(Cbor.encodeToHexString(Foo(0)))
// d844a163626172d85500
// D8 44           # tag(68)
//    A1           # map(1)
//       63        # text(3)
//          626172 # "bar"
//       D8 55     # tag(85)
//          00     # unsigned(0)
```

An inlined class with both its class and its value annotated with `@CborTag` will result in both being printed before
the value.

```kotlin
@JvmInline
@Serializable
@CborTag(1)
value class Foo(@CborTag(2) val bar: Int)
println(Cbor.encodeToHexString(Foo(3)))
// c1c203
// C1       # tag(1)
//    C2    # tag(2)
//       03 # unsigned(3)
```