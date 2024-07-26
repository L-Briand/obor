package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_MAP_INFINITE
import net.orandja.obor.codec.HEADER_MAP_START
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborMapEncoder(writer: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(writer, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_MAP_START
    override val infiniteToken: UByte = HEADER_MAP_INFINITE
}