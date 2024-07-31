package net.orandja.obor.codec.encoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_TEXT_INFINITE
import net.orandja.obor.codec.HEADER_TEXT_START
import net.orandja.obor.io.CborWriter

internal class CborTextEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: UByte = HEADER_TEXT_START
    override val infiniteToken: UByte = HEADER_TEXT_INFINITE

    // TODO: Restrict for strings only
}