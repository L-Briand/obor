package net.orandja.obor.codec.decoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.MAJOR_MAP
import net.orandja.obor.io.CborReader

internal class CborMapDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionDecoder(reader, serializersModule, parent) {
    override val major: Byte = MAJOR_MAP

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = super.decodeCollectionSize(descriptor) * 2
}