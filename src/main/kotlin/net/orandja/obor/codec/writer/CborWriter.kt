package net.orandja.obor.codec.writer

@ExperimentalUnsignedTypes
internal interface CborWriter {
    fun write(byte: UByte)
    fun write(bytes: UByteArray)
    fun writeMajor8(major: UByte, value: UByte)
    fun writeHeader8(header: UByte, value: UByte)
    fun writeMajor16(major: UByte, value: UShort)
    fun writeHeader16(header: UByte, value: UShort)
    fun writeMajor32(major: UByte, value: UInt)
    fun writeHeader32(header: UByte, value: UInt)
    fun writeMajor64(major: UByte, value: ULong)
    fun writeHeader64(header: UByte, value: ULong)
}