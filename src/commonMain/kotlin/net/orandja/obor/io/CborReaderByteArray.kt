package net.orandja.obor.io

/**
 * Implementation of [CborReader] specific to [ByteArray] as receiver.
 * If you want to quickly create a [CborReader] use [CborReader.ByReader] with [Reader.OfByteArray]
 */
internal class CborReaderByteArray(private val bytes: ByteArray) : CborReader {

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
        val result = (((bytes[position + 0].toInt() and 0xFF) shl 8) or
                (bytes[position + 1].toInt() and 0xFF)).toShort()
        position += 2
        return result
    }

    override fun nextInt(): Int {
        val result = (((bytes[position + 0].toInt() and 0xFF) shl 24) or
                ((bytes[position + 1].toInt() and 0xFF) shl 16) or
                ((bytes[position + 2].toInt() and 0xFF) shl 8) or
                (bytes[position + 3].toInt() and 0xFF))
        position += 4
        return result
    }

    override fun nextLong(): Long {
        val result = (((bytes[position + 0].toLong() and 0xFFL) shl 56) or
                ((bytes[position + 1].toLong() and 0xFFL) shl 48) or
                ((bytes[position + 2].toLong() and 0xFFL) shl 40) or
                ((bytes[position + 3].toLong() and 0xFFL) shl 32) or
                ((bytes[position + 4].toLong() and 0xFFL) shl 24) or
                ((bytes[position + 5].toLong() and 0xFFL) shl 16) or
                ((bytes[position + 6].toLong() and 0xFFL) shl 8) or
                (bytes[position + 7].toLong() and 0xFFL))
        position += 8
        return result
    }
}