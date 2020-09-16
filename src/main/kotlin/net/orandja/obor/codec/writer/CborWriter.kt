package net.orandja.obor.codec.writer

/**  */
@ExperimentalUnsignedTypes
internal interface CborWriter {
    /** write a byte */
    fun write(byte: UByte)

    /** write an array of bytes */
    fun write(bytes: UByteArray)

    // writeMajor -> fit into a smaller header if there is room for it.
    // writeHeader -> write the header as it.
    fun writeMajor8(major: UByte, value: UByte)
    fun writeHeader8(header: UByte, value: UByte)
    fun writeMajor16(major: UByte, value: UShort)
    fun writeHeader16(header: UByte, value: UShort)
    fun writeMajor32(major: UByte, value: UInt)
    fun writeHeader32(header: UByte, value: UInt)
    fun writeMajor64(major: UByte, value: ULong)
    fun writeHeader64(header: UByte, value: ULong)
}