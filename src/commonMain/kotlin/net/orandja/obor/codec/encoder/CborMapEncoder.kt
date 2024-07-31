package net.orandja.obor.codec.encoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_MAP_INFINITE
import net.orandja.obor.codec.HEADER_MAP_START
import net.orandja.obor.io.CborWriter

internal class CborMapEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: UByte = HEADER_MAP_START
    override val infiniteToken: UByte = HEADER_MAP_INFINITE

}