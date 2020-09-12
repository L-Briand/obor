package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_ARRAY_INFINITE
import net.orandja.obor.codec.HEADER_ARRAY_START
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborListEncoder(writer: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(writer, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_ARRAY_START
    override val infiniteToken: UByte = HEADER_ARRAY_INFINITE
}