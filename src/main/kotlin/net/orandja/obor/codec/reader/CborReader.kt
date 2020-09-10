package net.orandja.obor.codec.reader

@ExperimentalUnsignedTypes
internal interface CborReader {
    fun read(): UByte
    fun read(bytes: Int): UByteArray
    fun peek(): UByte
    fun consume()
    fun peekConsume(): UByte
    fun nextUByte(): UByte
    fun nextUShort(): UShort
    fun nextUInt(): UInt
    fun nextULong(): ULong
}