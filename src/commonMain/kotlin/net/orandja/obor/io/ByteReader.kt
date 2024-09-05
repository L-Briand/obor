package net.orandja.obor.io

interface ByteReader : NativeArrayReader<Byte, ByteArray> {

    @Throws(ReaderException::class)
    fun readString(count: Int): String

    /** Simple implementation to read from a [ByteArray] */
    class Of(private val bytes: ByteArray) : ByteReader {
        private var position = 0
        override fun totalRead(): Long = position.toLong()

        override fun read(): Byte {
            if (position >= bytes.size)
                throw ReaderException("Cannot read beyond the end of the byte array. (Size: ${bytes.size}, Index: $position)")
            return bytes[position++]
        }

        override fun read(count: Int): ByteArray {
            if (count == 0) return byteArrayOf()
            if (position + count > bytes.size)
                throw ReaderException("Cannot read beyond the end of the byte array. (Size: ${bytes.size}, Range: ${position..<count})")
            val result = bytes.copyOfRange(position, position + count)
            position += count
            return result
        }

        override fun readString(count: Int): String {
            if (count == 0) return ""
            if (position + count > bytes.size)
                throw ReaderException("Cannot read beyond the end of the byte array. (Size: ${bytes.size}, Range: ${position..<count})")
            val result = bytes.decodeToString(position, position + count)
            position += count
            return result
        }

        override fun skip(count: Int) {
            if (position + count > bytes.size)
                throw ReaderException("Cannot skip beyond the end of the byte array. (Size: ${bytes.size}, Range: ${position..<count})")
            position += count
        }
    }
}