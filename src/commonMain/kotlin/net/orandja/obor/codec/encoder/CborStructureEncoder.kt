package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.*
import net.orandja.obor.io.CborWriter

@OptIn(ExperimentalSerializationApi::class)
internal class CborStructureEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: UByte = HEADER_MAP_START
    override val infiniteToken: UByte = HEADER_MAP_INFINITE

    companion object {
        val NO_TRACK = newEncoderTracker()
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val tracker = this.tracker
        this.tracker = NO_TRACK
        if (!super.encodeElement(descriptor, index)) {
            this.tracker = tracker
            return false
        }
        encodeStructureName(descriptor.getElementName(index))
        this.tracker = tracker

        setFieldTracker(descriptor.getElementAnnotations(index))
        encodeTag()
        return true
    }
}