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

    override fun readAsString(count: Int): String {
        if (count == 0) return ""
        val result = array.decodeToString(position, position + count)
        position += count
        return result
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

    override fun nextByte(): Byte = read()

    override fun nextShort(): Short {
        val result = (((array[position + 0].toInt() and 0xFF) shl 8) or
                (array[position + 1].toInt() and 0xFF)).toShort()
        position += 2
        return result
    }

    override fun nextInt(): Int {
        val result = (((array[position + 0].toInt() and 0xFF) shl 24) or
                ((array[position + 1].toInt() and 0xFF) shl 16) or
                ((array[position + 2].toInt() and 0xFF) shl 8) or
                (array[position + 3].toInt() and 0xFF))
        position += 4
        return result
    }

    override fun nextLong(): Long {
        val result = (((array[position + 0].toLong() and 0xFFL) shl 56) or
                ((array[position + 1].toLong() and 0xFFL) shl 48) or
                ((array[position + 2].toLong() and 0xFFL) shl 40) or
                ((array[position + 3].toLong() and 0xFFL) shl 32) or
                ((array[position + 4].toLong() and 0xFFL) shl 24) or
                ((array[position + 5].toLong() and 0xFFL) shl 16) or
                ((array[position + 6].toLong() and 0xFFL) shl 8) or
                (array[position + 7].toLong() and 0xFFL))
        position += 8
        return result
    }

    override fun skip(count: Int) {
        consume()
        position += count
    }
}