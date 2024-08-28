package net.orandja.obor.io

/**
 * Something that can write into a native array.
 */
interface Writer<T, in arrayOfT> {
    fun write(value: T)
    fun write(array: arrayOfT, offset: Int, count: Int)

    /** Simple implementation to write into a [ByteArray] */
    class OfByteArray(private val byteArray: ByteArray) : Writer<Byte, ByteArray> {
        var position = 0

        override fun write(value: Byte) {
            if (position >= byteArray.size) throw WriterException(
                "Tried to write at position $position but is out of bound for array of size ${byteArray.size}"
            )
            byteArray[position++] = value
        }

        override fun write(array: ByteArray, offset: Int, count: Int) {
            if (count == 0) return
            if (count !in 0..array.size || count + offset !in 0..array.size) throw WriterException(
                "Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset}"
            )
            if (position + count > byteArray.size) throw WriterException(
                "Tried to write at ${position..<count} but is out of bound for array of size ${byteArray.size}"
            )

            array.copyInto(byteArray, position, offset, offset + count)
            position += count
        }
    }
}

inline fun Writer<*, ByteArray>.write(array: ByteArray) = write(array, 0, array.size)
inline fun Writer<*, ShortArray>.write(array: ShortArray) = write(array, 0, array.size)
inline fun Writer<*, IntArray>.write(array: IntArray) = write(array, 0, array.size)
inline fun Writer<*, LongArray>.write(array: LongArray) = write(array, 0, array.size)
inline fun Writer<*, FloatArray>.write(array: FloatArray) = write(array, 0, array.size)
inline fun Writer<*, DoubleArray>.write(array: DoubleArray) = write(array, 0, array.size)
inline fun Writer<*, BooleanArray>.write(array: BooleanArray) = write(array, 0, array.size)
inline fun Writer<*, CharArray>.write(array: CharArray) = write(array, 0, array.size)
inline fun Writer<*, Array<*>>.write(array: Array<*>) = write(array, 0, array.size)
inline fun Writer<*, Collection<*>>.write(array: Collection<*>) = write(array, 0, array.size)
