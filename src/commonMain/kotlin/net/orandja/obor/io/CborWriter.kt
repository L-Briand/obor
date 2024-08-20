package net.orandja.obor.io

import net.orandja.obor.codec.*
import net.orandja.obor.codec.SIZE_16
import net.orandja.obor.codec.SIZE_32
import net.orandja.obor.codec.SIZE_64
import net.orandja.obor.codec.SIZE_8
import net.orandja.obor.codec.SIZE_MASK
import kotlin.experimental.and
import kotlin.experimental.or

interface CborWriter : Writer<Byte, ByteArray> {
    // writeMajor -> fit into a smaller header if there is room for it.
    // writeHeader -> write the header + value as it.
    fun writeMajor8(major: Byte, value: Byte)
    fun writeHeader8(header: Byte, value: Byte)
    fun writeMajor16(major: Byte, value: Short)
    fun writeHeader16(header: Byte, value: Short)
    fun writeMajor32(major: Byte, value: Int)
    fun writeHeader32(header: Byte, value: Int)
    fun writeMajor64(major: Byte, value: Long)
    fun writeHeader64(header: Byte, value: Long)

    /** Use this class to quickly write a CborWriter. */
    open class ByWriter(writer: Writer<Byte, ByteArray>) : CborWriter, Writer<Byte, ByteArray> by writer {

        companion object {
            private const val SHORT_BYTE_MASK = 0xFF_00.toShort()
            private const val INT_SHORT_MASK = 0xFFFF_0000.toInt()
            private const val LONG_INT_MASK = -4294967296 // 0xFFFFFFFF_00000000u.toLong()
        }

        private val buffer: ByteArray = ByteArray(9)

        override fun writeMajor8(major: Byte, value: Byte) {
            if (value and SIZE_MASK < SIZE_8) write(major or value)
            else writeHeader8(major or SIZE_8, value)
        }

        override fun writeHeader8(header: Byte, value: Byte) {
            buffer[0] = header
            buffer[1] = value
            write(buffer, 0, 2)
        }

        override fun writeMajor16(major: Byte, value: Short) {
            if ((value and SHORT_BYTE_MASK) == 0.toShort()) writeMajor8(major, value.toByte())
            else writeHeader16(major or SIZE_16, value)
        }

        override fun writeHeader16(header: Byte, value: Short) {
            buffer[0] = header
            value.into(buffer, 1)
            write(buffer, 0, 3)
        }

        override fun writeMajor32(major: Byte, value: Int) {
            if (value and INT_SHORT_MASK == 0) writeMajor16(major, value.toShort())
            else writeHeader32(major or SIZE_32, value)
        }

        override fun writeHeader32(header: Byte, value: Int) {
            buffer[0] = header
            value.into(buffer, 1)
            write(buffer, 0, 5)
        }

        override fun writeMajor64(major: Byte, value: Long) {
            if (value and LONG_INT_MASK == 0L) writeMajor32(major, value.toInt())
            else writeHeader64(major or SIZE_64, value)
        }

        override fun writeHeader64(header: Byte, value: Long) {
            buffer[0] = header
            value.into(buffer, 1)
            write(buffer, 0, 9)
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
}