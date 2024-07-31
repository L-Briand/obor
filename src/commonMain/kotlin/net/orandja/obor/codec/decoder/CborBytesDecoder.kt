package net.orandja.obor.codec.decoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.io.CborReader

internal class CborBytesDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionDecoder(reader, serializersModule, parent) {
    override val major: UByte = MAJOR_BYTE

    // TODO: restrict elements to be only Bytes
}