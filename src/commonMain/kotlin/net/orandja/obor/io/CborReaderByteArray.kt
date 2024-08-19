package net.orandja.obor.io

/**
 * Implementation of [CborReader] for in memory ByteArray.
 */
internal class CborReaderByteArray(private val array: ByteArray) : CborReader {

    private var position = 0

    override fun read(): Byte = array[position++]

    override fun read(count: Int): ByteArray {
        if (count == 0) return ByteArray(0)
        val read = ByteArray(count)
        array.copyInto(read, 0, position, position + count)
        position += count
        return read
    }

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
        val result = (((array[position + 0].toInt() and 0xFF) shl 8) or
                (array[position + 1].toInt() and 0xFF)).toUShort()
        position += 2
        return result
    }

    override fun nextUInt(): UInt {
        val result = (((array[position + 0].toInt() and 0xFF) shl 24) or
                ((array[position + 1].toInt() and 0xFF) shl 16) or
                ((array[position + 2].toInt() and 0xFF) shl 8) or
                (array[position + 3].toInt() and 0xFF)).toUInt()
        position += 4
        return result
    }

    override fun nextULong(): ULong {
        val result = (((array[position + 0].toLong() and 0xFF) shl 56) or
                ((array[position + 1].toLong() and 0xFF) shl 48) or
                ((array[position + 2].toLong() and 0xFF) shl 40) or
                ((array[position + 3].toLong() and 0xFF) shl 32) or
                ((array[position + 4].toLong() and 0xFF) shl 24) or
                ((array[position + 5].toLong() and 0xFF) shl 16) or
                ((array[position + 6].toLong() and 0xFF) shl 8) or
                (array[position + 7].toLong() and 0xFF)).toULong()
        position += 8
        return result
    }

    override fun skip(bytes: Int) {
        consume()
        position += bytes
    }
}