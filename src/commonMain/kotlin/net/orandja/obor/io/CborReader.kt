package net.orandja.obor.io

interface CborReader {

    fun peek(): Byte
    fun consume()
    fun peekConsume(): Byte

    /** Always read the next byte. Should not return peeked value. */
    fun read(): Byte
    fun read(bytes: Int): ByteArray

    fun nextUByte(): UByte
    fun nextUShort(): UShort
    fun nextUInt(): UInt
    fun nextULong(): ULong

    fun skip(bytes: Int)
}