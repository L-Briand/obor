package net.orandja.obor.io

interface CborWriter {
    fun write(byte: Byte)
    fun write(bytes: ByteArray, offset: Int, count: Int)

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
}