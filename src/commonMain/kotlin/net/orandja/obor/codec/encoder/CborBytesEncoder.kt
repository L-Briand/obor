package net.orandja.obor.codec.encoder

import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BYTE_INFINITE
import net.orandja.obor.codec.HEADER_BYTE_START
import net.orandja.obor.io.CborWriter

internal class CborBytesEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: UByte = HEADER_BYTE_START
    override val infiniteToken: UByte = HEADER_BYTE_INFINITE

    // TODO: Restrict for bytes only
}