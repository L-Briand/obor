package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_ARRAY
import net.orandja.obor.codec.reader.CborReader

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborListDecoder(input: CborReader, serializersModule: SerializersModule) :
    CborCollectionDecoder(input, serializersModule) {
    override val major: UByte = MAJOR_ARRAY
}