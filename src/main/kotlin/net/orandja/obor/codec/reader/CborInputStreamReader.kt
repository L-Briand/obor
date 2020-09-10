package net.orandja.obor.codec.reader

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import java.io.InputStream


@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborInputStreamReader(private val input: InputStream) : CborReader {
    private var buffer = UByteArray(8) { 0u }
    private var peek: UByte? = null
    override fun peek(): UByte = peek ?: run {
        peek = input.read().toUByte()
        peek!!
    }

    override fun consume() {
        peek = null
    }

    override fun peekConsume(): UByte = peek().also { consume() }
    override fun read(): UByte = input.read().toUByte()
    override fun read(bytes: Int): UByteArray = input.readNBytes(bytes).asUByteArray()
    override fun nextUByte(): UByte = read()
    override fun nextUShort(): UShort {
        readInBuffer(2)
        val i = ((buffer[0].toInt() and 0xFF) shl 8) or
            (buffer[1].toInt() and 0xFF)
        return i.toUShort()
    }

    override fun nextUInt(): UInt {
        readInBuffer(4)
        val i = ((buffer[0].toInt() and 0xFF) shl 24) or
            ((buffer[1].toInt() and 0xFF) shl 16) or
            ((buffer[2].toInt() and 0xFF) shl 8) or
            (buffer[3].toInt() and 0xFF)
        return i.toUInt()
    }

    override fun nextULong(): ULong {
        readInBuffer(8)
        val i = ((buffer[0].toLong() and 0xFF) shl 56) or
            ((buffer[1].toLong() and 0xFF) shl 48) or
            ((buffer[2].toLong() and 0xFF) shl 40) or
            ((buffer[3].toLong() and 0xFF) shl 32) or
            ((buffer[4].toLong() and 0xFF) shl 24) or
            ((buffer[5].toLong() and 0xFF) shl 16) or
            ((buffer[6].toLong() and 0xFF) shl 8) or
            (buffer[7].toLong() and 0xFF)
        return i.toULong()
    }

    private fun readInBuffer(bytes: Int) {
        var read = 0
        while (read < bytes) {
            val i = input.read(buffer.asByteArray(), read, bytes - read)
            if (i == -1) error("Unexpected EOF")
            read += i
        }
    }

}