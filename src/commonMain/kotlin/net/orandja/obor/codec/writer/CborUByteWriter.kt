package net.orandja.obor.codec.writer

import kotlinx.serialization.InternalSerializationApi
import net.orandja.obor.codec.SIZE_16
import net.orandja.obor.codec.SIZE_32
import net.orandja.obor.codec.SIZE_64
import net.orandja.obor.codec.SIZE_8
import net.orandja.obor.vector.UByteVector

/** Implementation of [CborWriter] for in memory ByteArray */
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborUByteWriter(private val delegate: UByteVector) : CborWriter {
    private val buffer = UByteArray(9) { 0u }
    override fun write(byte: UByte) = delegate.add(byte)
    override fun write(bytes: UByteArray) = delegate.add(bytes, 0, bytes.size)

    override fun writeMajor8(major: UByte, value: UByte) {
        if (value < SIZE_8) delegate.add(major or value)
        else writeHeader8(major or SIZE_8, value)
    }

    override fun writeHeader8(header: UByte, value: UByte) {
        buffer[0] = header
        buffer[1] = value
        delegate.add(buffer, 0, 2)
    }

    override fun writeMajor16(major: UByte, value: UShort) {
        if (value <= UByte.MAX_VALUE) writeMajor8(major, value.toUByte())
        else writeHeader16(major or SIZE_16, value)
    }

    override fun writeHeader16(header: UByte, value: UShort) {
        buffer[0] = header
        value.into(buffer, 1)
        delegate.add(buffer, 0, 3)
    }

    override fun writeMajor32(major: UByte, value: UInt) {
        if (value <= UShort.MAX_VALUE) writeMajor16(major, value.toUShort())
        else writeHeader32(major or SIZE_32, value)
    }

    override fun writeHeader32(header: UByte, value: UInt) {
        buffer[0] = header
        value.into(buffer, 1)
        delegate.add(buffer, 0, 5)
    }

    override fun writeMajor64(major: UByte, value: ULong) {
        if (value <= UInt.MAX_VALUE) writeMajor32(major, value.toUInt())
        else writeHeader64(major or SIZE_64, value)
    }

    override fun writeHeader64(header: UByte, value: ULong) {
        buffer[0] = header
        value.into(buffer, 1)
        delegate.add(buffer, 0, 9)
    }

    private fun UShort.into(buffer: UByteArray, offset: Int) {
        buffer[0 + offset] = ((this.toUInt() shr 8) and 0xFFu).toUByte()
        buffer[1 + offset] = (this and 0xFFu).toUByte()
    }

    private fun UInt.into(buffer: UByteArray, offset: Int) {
        buffer[0 + offset] = (this shr 24 and 0xFFu).toUByte()
        buffer[1 + offset] = (this shr 16 and 0xFFu).toUByte()
        buffer[2 + offset] = (this shr 8 and 0xFFu).toUByte()
        buffer[3 + offset] = (this and 0xFFu).toUByte()
    }

    private fun ULong.into(buffer: UByteArray, offset: Int) {
        buffer[0 + offset] = (this shr 56 and 0xFFu).toUByte()
        buffer[1 + offset] = (this shr 48 and 0xFFu).toUByte()
        buffer[2 + offset] = (this shr 40 and 0xFFu).toUByte()
        buffer[3 + offset] = (this shr 32 and 0xFFu).toUByte()
        buffer[4 + offset] = (this shr 24 and 0xFFu).toUByte()
        buffer[5 + offset] = (this shr 16 and 0xFFu).toUByte()
        buffer[6 + offset] = (this shr 8 and 0xFFu).toUByte()
        buffer[7 + offset] = (this and 0xFFu).toUByte()
    }
}