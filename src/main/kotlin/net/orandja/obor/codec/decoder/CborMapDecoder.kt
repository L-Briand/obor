package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_MAP
import net.orandja.obor.codec.reader.CborReader

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborMapDecoder(input: CborReader, serializersModule: SerializersModule) :
    CborCollectionDecoder(input, serializersModule) {
    override val major: UByte = MAJOR_MAP

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = super.decodeCollectionSize(descriptor) * 2
}