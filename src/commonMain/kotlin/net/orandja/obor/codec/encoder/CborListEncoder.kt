package net.orandja.obor.codec.encoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_ARRAY_INFINITE
import net.orandja.obor.codec.HEADER_ARRAY_START
import net.orandja.obor.io.CborWriter

internal class CborListEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: Byte = HEADER_ARRAY_START
    override val infiniteToken: Byte = HEADER_ARRAY_INFINITE
}