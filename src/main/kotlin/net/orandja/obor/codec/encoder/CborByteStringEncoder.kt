package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BYTE_INFINITE
import net.orandja.obor.codec.HEADER_BYTE_START
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@ExperimentalUnsignedTypes
@InternalSerializationApi
internal class CborByteStringEncoder(out: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(out, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_BYTE_START
    override val infiniteToken: UByte = HEADER_BYTE_INFINITE

    private var index: Int = 0

    override fun encodeByte(value: Byte) {
        if (!isFinite) {
            if (index == 0) out.writeMajor32(MAJOR_BYTE, chunkSize.toUInt())
            index += 1
            if (index == chunkSize) index = 0
        }
        out.write(value.toUByte())
    }
}