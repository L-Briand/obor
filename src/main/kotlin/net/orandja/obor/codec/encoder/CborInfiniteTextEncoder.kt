package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_TEXT_INFINITE
import net.orandja.obor.codec.HEADER_TEXT_START
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@ExperimentalUnsignedTypes
@InternalSerializationApi
internal class CborInfiniteTextEncoder(writer: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(writer, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_TEXT_START
    override val infiniteToken: UByte = HEADER_TEXT_INFINITE

    // TODO: Restrict for strings only
}