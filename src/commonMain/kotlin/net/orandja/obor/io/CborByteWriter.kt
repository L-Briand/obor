package net.orandja.obor.io

import net.orandja.obor.codec.SIZE_16
import net.orandja.obor.codec.SIZE_32
import net.orandja.obor.codec.SIZE_64
import net.orandja.obor.codec.SIZE_8
import kotlin.experimental.and
import kotlin.experimental.or

/** Implementation of [CborWriter] for in memory ByteArray */
class CborByteWriter(private val delegate: ByteVector) : CborWriter {

    companion object {
        private const val BYTE_MASK = 0xE0.toByte()
        private const val SHORT_BYTE_MASK = 0xFF_00.toShort()
        private const val INT_SHORT_MASK = 0xFFFF_0000.toInt()
        private const val LONG_INT_MASK = -4294967296 // 0xFFFFFFFF_00000000u.toLong()
    }

    override fun write(byte: Byte) = delegate.add(byte)
    override fun write(bytes: ByteArray, offset: Int, count: Int) = delegate.add(bytes, offset, count)

    override fun writeMajor8(major: Byte, value: Byte) {
        if (0 <= value && value < SIZE_8) delegate.add(major or value)
        else writeHeader8(major or SIZE_8, value)
    }

    override fun writeHeader8(header: Byte, value: Byte) {
        delegate.ensureCapacity(2)
        delegate[delegate.size] = header
        delegate[delegate.size + 1] = value
        delegate.size += 2
    }

    override fun writeMajor16(major: Byte, value: Short) {
        if ((value and SHORT_BYTE_MASK) == 0.toShort()) writeMajor8(major, value.toByte())
        else writeHeader16(major or SIZE_16, value)
    }

    override fun writeHeader16(header: Byte, value: Short) {
        delegate.ensureCapacity(3)
        delegate[delegate.size] = header
        value.into(delegate.array, delegate.size + 1)
        delegate.size += 3
    }

    override fun writeMajor32(major: Byte, value: Int) {
        if (value and INT_SHORT_MASK == 0) writeMajor16(major, value.toShort())
        else writeHeader32(major or SIZE_32, value)
    }

    override fun writeHeader32(header: Byte, value: Int) {
        delegate.ensureCapacity(5)
        delegate[delegate.size] = header
        value.into(delegate.array, delegate.size + 1)
        delegate.size += 5
    }

    override fun writeMajor64(major: Byte, value: Long) {
        if (value and LONG_INT_MASK == 0L) writeMajor32(major, value.toInt())
        else writeHeader64(major or SIZE_64, value)
    }

    override fun writeHeader64(header: Byte, value: Long) {
        delegate.ensureCapacity(9)
        delegate[delegate.size] = header
        value.into(delegate.array, delegate.size + 1)
        delegate.size += 9
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Short.into(buffer: ByteArray, offset: Int) {
        buffer[0 + offset] = ((this.toInt() shr 8) and 0xFF).toByte()
        buffer[1 + offset] = (this and 0xFF).toByte()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.into(buffer: ByteArray, offset: Int) {
        buffer[0 + offset] = (this shr 24 and 0xFF).toByte()
        buffer[1 + offset] = (this shr 16 and 0xFF).toByte()
        buffer[2 + offset] = (this shr 8 and 0xFF).toByte()
        buffer[3 + offset] = (this and 0xFF).toByte()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Long.into(buffer: ByteArray, offset: Int) {
        buffer[0 + offset] = (this shr 56 and 0xFF).toByte()
        buffer[1 + offset] = (this shr 48 and 0xFF).toByte()
        buffer[2 + offset] = (this shr 40 and 0xFF).toByte()
        buffer[3 + offset] = (this shr 32 and 0xFF).toByte()
        buffer[4 + offset] = (this shr 24 and 0xFF).toByte()
        buffer[5 + offset] = (this shr 16 and 0xFF).toByte()
        buffer[6 + offset] = (this shr 8 and 0xFF).toByte()
        buffer[7 + offset] = (this and 0xFF).toByte()
    }
}