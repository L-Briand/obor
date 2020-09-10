package net.orandja.obor.codec.reader

@ExperimentalUnsignedTypes
internal class CborUByteReader(private val array: UByteArray) : CborReader {
    private var position = 0
    override fun read(): UByte = array[position]
        .also { position++ }

    override fun read(bytes: Int): UByteArray {
        val read = UByteArray(bytes)
        array.copyInto(read, 0, position, position + bytes)
        position += bytes
        return read
    }

    private var peek: UByte? = null
    override fun peek(): UByte = peek ?: read().also { peek = it }

    override fun consume() {
        peek = null
    }

    override fun peekConsume(): UByte = peek().also { consume() }

    override fun nextUByte(): UByte = read()

    override fun nextUShort(): UShort = (((array[position + 0].toUInt() and 0xFFu) shl 8) or
        (array[position + 1].toUInt() and 0xFFu)).toUShort()
        .also { position += 1 }

    override fun nextUInt(): UInt = (((array[position + 0].toUInt() and 0xFFu) shl 24) or
        ((array[position + 1].toUInt() and 0xFFu) shl 16) or
        ((array[position + 2].toUInt() and 0xFFu) shl 8) or
        (array[position + 3].toUInt() and 0xFFu))
        .also { position += 4 }

    override fun nextULong(): ULong = (((array[position + 0].toULong() and 0xFFu) shl 56) or
        ((array[position + 1].toULong() and 0xFFu) shl 48) or
        ((array[position + 2].toULong() and 0xFFu) shl 40) or
        ((array[position + 3].toULong() and 0xFFu) shl 32) or
        ((array[position + 4].toULong() and 0xFFu) shl 24) or
        ((array[position + 5].toULong() and 0xFFu) shl 16) or
        ((array[position + 6].toULong() and 0xFFu) shl 8) or
        (array[position + 7].toULong() and 0xFFu))
        .also { position += 8 }
}