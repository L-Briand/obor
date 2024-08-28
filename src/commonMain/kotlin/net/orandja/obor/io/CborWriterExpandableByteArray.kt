package net.orandja.obor.io

import net.orandja.obor.codec.SIZE_16
import net.orandja.obor.codec.SIZE_32
import net.orandja.obor.codec.SIZE_64
import net.orandja.obor.codec.SIZE_8
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Implementation of [CborWriter] specific to [ExpandableByteArray] as receiver.
 * If you want to quickly create a [CborWriter] use [CborWriter.ByWriter] with [Writer.OfByteArray]
 */
internal class CborWriterExpandableByteArray(private val vector: ExpandableByteArray) : CborWriter, Writer<Byte, ByteArray> by vector {

    companion object {
        private const val SHORT_BYTE_MASK = 0xFF_00.toShort()
        private const val INT_SHORT_MASK = 0xFFFF_0000.toInt()
        private const val LONG_INT_MASK = -4294967296 // 0xFFFFFFFF_00000000u.toLong()
    }

    override fun writeMajor8(major: Byte, value: Byte) {
        if (value in 0..<SIZE_8) vector.write(major or value)
        else writeHeader8(major or SIZE_8, value)
    }

    override fun writeHeader8(header: Byte, value: Byte) {
        vector.ensureCapacity(2)
        vector.array[vector.size] = header
        vector.array[vector.size + 1] = value
        vector.size += 2
    }

    override fun writeMajor16(major: Byte, value: Short) {
        if ((value and SHORT_BYTE_MASK) == 0.toShort()) writeMajor8(major, value.toByte())
        else writeHeader16(major or SIZE_16, value)
    }

    override fun writeHeader16(header: Byte, value: Short) {
        vector.ensureCapacity(3)
        vector.array[vector.size] = header
        value.into(vector.array, vector.size + 1)
        vector.size += 3
    }

    override fun writeMajor32(major: Byte, value: Int) {
        if (value and INT_SHORT_MASK == 0) writeMajor16(major, value.toShort())
        else writeHeader32(major or SIZE_32, value)
    }

    override fun writeHeader32(header: Byte, value: Int) {
        vector.ensureCapacity(5)
        vector.array[vector.size] = header
        value.into(vector.array, vector.size + 1)
        vector.size += 5
    }

    override fun writeMajor64(major: Byte, value: Long) {
        if (value and LONG_INT_MASK == 0L) writeMajor32(major, value.toInt())
        else writeHeader64(major or SIZE_64, value)
    }

    override fun writeHeader64(header: Byte, value: Long) {
        vector.ensureCapacity(9)
        vector.array[vector.size] = header
        value.into(vector.array, vector.size + 1)
        vector.size += 9
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