package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_MAP_INFINITE
import net.orandja.obor.codec.HEADER_MAP_START
import net.orandja.obor.codec.encFieldSkipTag
import net.orandja.obor.io.CborWriter

@OptIn(ExperimentalSerializationApi::class)
internal class CborStructureEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: Byte = HEADER_MAP_START
    override val infiniteToken: Byte = HEADER_MAP_INFINITE

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        tracker.encFieldSkipTag = true
        if (!super.encodeElement(descriptor, index)) {
            tracker.encFieldSkipTag = false
            return false
        }
        encodeRawString(descriptor.getElementName(index).encodeToByteArray())
        tracker.encFieldSkipTag = false

        setFieldTracker(descriptor.getElementAnnotations(index))
        encodeTag()
        return true
    }
}