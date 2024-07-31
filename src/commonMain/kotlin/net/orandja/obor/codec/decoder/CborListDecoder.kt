package net.orandja.obor.codec.decoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_ARRAY
import net.orandja.obor.io.CborReader

internal class CborListDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionDecoder(reader, serializersModule, parent) {
    override val major: UByte = MAJOR_ARRAY
}