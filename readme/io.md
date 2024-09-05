# Reading and writing to anything

The default behavior for reading and writing CBOR data with kotlinx-serialization is to have a single ByteArray
containing the whole message. But what if you need to deserialize multiple messages in a row. Have the size of the
decoded message you just deserialized, or the size of the encoded message you serialized.

Well, I thought about it and added abstraction on that.

## Reading from anything

To read a CBOR message, there are two layers of abstraction.

- The ability to read and skip bytes without
  backtracking ([ByteReader](../src/commonMain/kotlin/net/orandja/obor/io/ByteReader.kt)).
  You can create a `ByteReader` from a `ByteArray` with `ByteReader.Of(byteArray)`
- The ability to keep the last read byte and read complex
  data ([CborReader.kt](../src/commonMain/kotlin/net/orandja/obor/io/CborReader.kt))
  You can create a `CborReader` from a `ByteReader` with `CborReader.ByReader(byteReader)`

Internally, the Cbor codec uses the
class [CborReaderByteArray.kt](../src/commonMain/kotlin/net/orandja/obor/io/specific/CborReaderByteArray.kt) which
combines both interfaces for efficiency

Then you can then use the created `CborReader` and pass it to `Cbor.decodeFromReader`. The main advantage of this is to
keep track of bytes read with `ByteReader.totalRead()` and the ability to read multiple CBOR messages in a row with a
single Byte array.

## Writing from anything

To write a CBOR message, there are also two layers of abstraction.

- The ability to write bytes without
  backtracking ([ByteWriter](../src/commonMain/kotlin/net/orandja/obor/io/ByteWriter.kt)).
  You can create a `ByteWriter` to a `ByteArray` with `ByteWriter.Of(byteArray)`
- The ability to write CBOR specific headers and major
  information ([CborWriter.kt](../src/commonMain/kotlin/net/orandja/obor/io/CborWriter.kt))
  You can create a `CborWriter` from a `ByteWriter` with `CborReader.ByWriter(byteReader)`

If you are not constrained with memory, you can use
the [ExpandableByteArray.kt](../src/commonMain/kotlin/net/orandja/obor/io/specific/ExpandableByteArray.kt) which is a
`ByteWriter` with growing `ByteArray` inside.

Internally, the Cbor codec uses the
class [CborWriterExpandableByteArray.kt](../src/commonMain/kotlin/net/orandja/obor/io/specific/CborWriterExpandableByteArray.kt)
which combines both interfaces for efficiency.

Then you can then use the created `CborWriter` and pass it to `Cbor.encodeToWriter`. Like the reader, you keep track of
written bytes with `ByteReader.totalWrite()` and the ability to write multiple CBOR messages in a row to a single Byte
array.

## Quick implementations

A really simple `ByteReader` from a `ByteArray` without checking for errors can be created like this:

```kotlin
class ByteArrayReader(val array: ByteArray) : ByteReader {
  private var position = 0
  override fun totalRead(): Long = position.toLong()
  override fun read(): Byte = array[position++]
  override fun read(count: Int): ByteArray = array.copyOfRange(position, position + count).also { position += count }
  override fun readString(count: Int): String = array.decodeToString(position, position + count).also { position += count }
  override fun skip(count: Int) { position += count }
}
```

A really simple `ByteWriter` to a `ByteArray` without checking for errors can be created like this:

```kotlin
class ByteArrayWriter(val array: ByteArray) : ByteWriter {
  var position = 0
  override fun write(value: Byte) {
    array[position++] = value
  }

  override fun write(array: ByteArray, offset: Int, count: Int) {
    array.copyInto(this.array, position, offset, offset + count)
    position += count
  }

  override fun totalWrite(): Long {
    return position.toLong()
  }
}
```

