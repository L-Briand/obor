package net.orandja.obor.io

interface ByteWriter : NativeArrayWriter<Byte, ByteArray> {

    fun write(byteArray: ByteArray) = write(byteArray, 0, byteArray.size)

    /** Simple implementation to write into a [ByteArray] */
    class Of(private val byteArray: ByteArray) : ByteWriter {
        private var position = 0

        override fun totalWrite(): Long = position.toLong()

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