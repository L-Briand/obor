package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_TEXT
import net.orandja.obor.codec.reader.CborReader

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborInfiniteTextDecoder(reader: CborReader, serializersModule: SerializersModule) :
    CborCollectionDecoder(reader, serializersModule) {
    override val major: UByte = MAJOR_TEXT

    // TODO: restrict elements to be only Strings
}