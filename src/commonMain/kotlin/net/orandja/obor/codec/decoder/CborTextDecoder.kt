package net.orandja.obor.codec.decoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_TEXT
import net.orandja.obor.io.CborReader

internal class CborTextDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionDecoder(reader, serializersModule, parent) {
    override val major: Byte = MAJOR_TEXT

    // TODO: restrict elements to be only Strings
}