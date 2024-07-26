package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.Descriptors
import net.orandja.obor.codec.HEADER_MAP_INFINITE
import net.orandja.obor.codec.HEADER_MAP_START
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborStructureEncoder(writer: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(writer, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_MAP_START
    override val infiniteToken: UByte = HEADER_MAP_INFINITE

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        super.encodeString(descriptor.getElementName(index))
        // check for special feature fields
        chunkSize = (descriptor.getElementAnnotations(index).find { it is CborInfinite } as? CborInfinite)?.chunkSize ?: -1
        isRawBytes = descriptor.getElementAnnotations(index).any { it is CborRawBytes }
        encodeTag((descriptor.getElementAnnotations(index).find { it is CborTag } as? CborTag)?.tag ?: -1)
        return true
    }

    override fun encodeString(value: String) {
        val chunkSize = if (chunkSize == 0) 1 else chunkSize
        if (chunkSize in value.indices) { // String field is
            encodeStructure(Descriptors.infiniteText) {
                value.chunked(chunkSize).forEachIndexed { idx, it ->
                    this.encodeStringElement(Descriptors.string, idx, it)
                }
            }
        } else super.encodeString(value)
    }
}