package net.orandja.obor.io

interface CborReader : Reader<Byte, ByteArray> {
    fun peek(): Byte
    fun consume()
    fun peekConsume(): Byte

    fun nextUByte(): UByte
    fun nextUShort(): UShort
    fun nextUInt(): UInt
    fun nextULong(): ULong

    /** Use this class to quickly write a CborReader. */
    open class ByReader(private val reader: Reader<Byte, ByteArray>) : CborReader, Reader<Byte, ByteArray> by reader {
        private var peek: Byte? = null
        override fun peek(): Byte {
            if (peek != null) return peek!!
            peek = read()
            return peek!!
        }

        override fun consume() {
            peek = null
        }

        override fun peekConsume(): Byte {
            val result = peek()
            consume()
            return result
        }

        override fun nextUByte(): UByte = read().toUByte()
        override fun nextUShort(): UShort {
            val array = read(2)
            val result = (((array[0].toInt() and 0xFF) shl 8) or
                    (array[1].toInt() and 0xFF)).toUShort()
            return result
        }

        override fun nextUInt(): UInt {
            val array = read(4)
            val result = (((array[0].toInt() and 0xFF) shl 24) or
                    ((array[1].toInt() and 0xFF) shl 16) or
                    ((array[2].toInt() and 0xFF) shl 8) or
                    (array[3].toInt() and 0xFF)).toUInt()
            return result
        }

        override fun nextULong(): ULong {
            val array = read(8)
            val result = (((array[0].toLong() and 0xFF) shl 56) or
                    ((array[1].toLong() and 0xFF) shl 48) or
                    ((array[2].toLong() and 0xFF) shl 40) or
                    ((array[3].toLong() and 0xFF) shl 32) or
                    ((array[4].toLong() and 0xFF) shl 24) or
                    ((array[5].toLong() and 0xFF) shl 16) or
                    ((array[6].toLong() and 0xFF) shl 8) or
                    (array[7].toLong() and 0xFF)).toULong()
            return result
        }

        override fun skip(count: Int) {
            consume()
            reader.skip(count)
        }
    }
}