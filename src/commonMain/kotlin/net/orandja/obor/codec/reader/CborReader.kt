package net.orandja.obor.codec.reader

/** An interface who isolate simple actions from the source its reading */
@ExperimentalUnsignedTypes
internal interface CborReader {

    /** take a look at the next byte without consuming it. */
    fun peek(): UByte

    /** consume the peeked byte */
    fun consume()

    /** assemble both [peek] and [consume] */
    fun peekConsume(): UByte = peek().also { consume() }

    /** read next raw byte. Should not return peeked value. */
    fun read(): UByte

    /** read next n'th byte */
    fun read(bytes: Int): UByteArray

    /** read next bytes same as [read] */
    fun nextUByte(): UByte

    /** read next 2 bytes as short */
    fun nextUShort(): UShort

    /** read next 4 bytes as int */
    fun nextUInt(): UInt

    /** read next 8 bytes as long */
    fun nextULong(): ULong
}