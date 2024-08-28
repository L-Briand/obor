package net.orandja.obor.io


/** Something that can read in a native array */
interface Reader<T, arrayOfT> {
    fun totalRead(): Long

    @Throws(ReaderException::class)
    fun read(): T

    @Throws(ReaderException::class)
    fun read(count: Int): arrayOfT

    @Throws(ReaderException::class)
    fun readAsString(count: Int): String

    @Throws(ReaderException::class)
    fun skip(count: Int)

    /** Simple implementation to read from a [ByteArray] */
    class OfByteArray(private val bytes: ByteArray) : Reader<Byte, ByteArray> {
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

        override fun readAsString(count: Int): String {
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