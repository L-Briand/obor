# Unsigned values

Kotlin serialization api does not encode unsigned values *correctly*. For example, when a `UShort` is encoded a `Short`
value is given. So there is no way to distinguish between a normal short value or an unsigned one. By default, the
codec will automatically choose on major positive or major negative depending on the short sign.

If you try to encode a `UByte` over 127, it will encode it as negative.

```kotlin
println(Cbor.encodeToHexString(200u.toUByte())) // 3837 # negative(55)
```

If you require a strict policy on some of your data, you can use one of the many unsigned
serializer [CborUnsignedSerializer.kt](src/commonMain/kotlin/net/orandja/obor/serializer/CborUnsignedSerializer.kt).

```
CborUnsignedSerializer.UByte
CborUnsignedSerializer.UShort
CborUnsignedSerializer.UInt
CborUnsignedSerializer.ULong
CborUnsignedSerializer.UByteNeg
CborUnsignedSerializer.UShortNeg
CborUnsignedSerializer.UIntNeg
CborUnsignedSerializer.ULongNeg
```

Example:

```kotlin
@Serializable
data class Foo(@Serializable(CborUnsignedSerializer.UIntNeg::class) val foo: UInt)

println(Cbor.encodeToHexString(CborUnsignedSerializer.UByte, 0xFFu))
// 18ff 
// 18 FF # unsigned (255)

println(Cbor.encodeToHexString(CborUnsignedSerializer.UShortNeg, 0xFFFFu))
// 39ffff 
// 39 FFFF # negative(65535)

println(Cbor.encodeToHexString(Foo(0xFFFFFFFFu)))
// a163666f6f3affffffff
// A1             # map(1)
//    63          # text(3)
//       666F6F   # "foo"
//    3A FFFFFFFF # negative(4294967295)
```