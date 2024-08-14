package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborSkip
import net.orandja.obor.codec.MAJOR_MAP
import net.orandja.obor.codec.decClassHasTag
import net.orandja.obor.io.CborReader


@OptIn(ExperimentalSerializationApi::class)
internal class CborStructureDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionDecoder(reader, serializersModule, parent) {
    override val major: Byte = MAJOR_MAP

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (super.decodeElementIndex(descriptor) == CompositeDecoder.DECODE_DONE) return CompositeDecoder.DECODE_DONE
        val hasTag = tracker.decClassHasTag
        tracker.decClassHasTag = false
        var index = descriptor.getElementIndex(decodeString())
        var annotations = descriptor.getElementAnnotations(index)

        // - Decoded element not inside the kotlin object representation
        // - Element is explicitly skipped
        while (index == CompositeDecoder.UNKNOWN_NAME ||
            annotations.any { it is CborSkip }
        ) {
            // The field name is fetched by decodeString() when getting index
            // This discards the value element.
            skipElement()
            if (super.decodeElementIndex(descriptor) == CompositeDecoder.DECODE_DONE) return CompositeDecoder.DECODE_DONE
            index = descriptor.getElementIndex(decodeString())
            annotations = descriptor.getElementAnnotations(index)
        }
        tracker.decClassHasTag = hasTag
        readTag(descriptor.getElementAnnotations(index))
        decodeTags()
        return index
    }
}


