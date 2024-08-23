package net.orandja.obor.io


/** Something that can read in a native array */
interface Reader<T, arrayOfT> {
    fun read(): T
    fun read(count: Int): arrayOfT
    fun readAsString(count: Int): String
    fun skip(count: Int)

    /** Simple implementation to read from a [ByteArray] */
    class OfByteArray(private val bytes: ByteArray) : Reader<Byte, ByteArray> {
        private var position = 0

        override fun read(): Byte {
            if (position >= bytes.size) throw IndexOutOfBoundsException("Cannot read beyond the end of the byte array.")
            return bytes[position++]
        }

        override fun read(count: Int): ByteArray {
            if (count == 0) return byteArrayOf()
            if (position + count > bytes.size) throw IndexOutOfBoundsException("Cannot read beyond the end of the byte array.")
            val result = bytes.copyOfRange(position, position + count)
            position += count
            return result
        }

        override fun readAsString(count: Int): String {
            if (count == 0) return ""
            if (position + count > bytes.size) throw IndexOutOfBoundsException("Cannot read beyond the end of the byte array.")
            val result = bytes.decodeToString(position, position + count)
            position += count
            return result
        }

        override fun skip(count: Int) {
            if (position + count > bytes.size) throw IndexOutOfBoundsException("Cannot skip beyond the end of the byte array.")
            position += count
        }
    }
}