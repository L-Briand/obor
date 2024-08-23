package net.orandja.obor.io

/**
 * Something that can write into a native array.
 */
interface Writer<T, arrayOfT> {
    fun write(value: T)
    fun write(array: arrayOfT, offset: Int, count: Int)

    /** Simple implementation to write into a [ByteArray] */
    class OfByteArray(private val byteArray: ByteArray) : Writer<Byte, ByteArray> {
        private var position = 0
        override fun write(value: Byte) {
            if (position >= byteArray.size) throw IndexOutOfBoundsException(
                "Tried to write at position $position but is out of bound for array of size ${byteArray.size}"
            )
            byteArray[position++] = value
        }

        override fun write(array: ByteArray, offset: Int, count: Int) {
            if (count !in 0..array.size || count + offset !in 0..array.size) throw IndexOutOfBoundsException(
                "Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset}"
            )
            if (position + count > byteArray.size) throw IndexOutOfBoundsException(
                "Tried to write at ${position..<count} but is out of bound for array of size ${byteArray.size}"
            )

            array.copyInto(byteArray, position, offset, offset + count)
            position += count
        }
    }
}