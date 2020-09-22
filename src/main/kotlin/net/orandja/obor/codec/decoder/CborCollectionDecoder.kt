package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.SIZE_INFINITE
import net.orandja.obor.codec.hasMajor
import net.orandja.obor.codec.reader.CborReader

/** Base class for decoding all [StructureKind] */
@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal abstract class CborCollectionDecoder(
    reader: CborReader,
    serializersModule: SerializersModule
) : CborDecoder(reader, serializersModule) {

    protected var isStructureInfinite: Boolean = false

    /** used by beginStructure to know the start of the structure has already been read */
    private var beginDone = false

    /** used by endStructure to know the end of the structure has already been read */
    private var endDone = false

    /** Used to know the number of elements currently decoded */
    private var indexCounter = 0

    /** Size of elements inside this structure -1 is infinite */
    private var size = -1

    /**
     * Major kind of the structure expected to decode.
     * Throw an exception if it do not match the expected type
     */
    abstract val major: UByte

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        // if another structure is start inside this one
        // then delegate decoding by a new collection decoder created in the super function.
        if (beginDone) return super.beginStructure(descriptor)
        requiredTag = (descriptor.annotations.find { it is CborTag } as? CborTag)?.let { if (it.require) it.tag else -1 } ?: requiredTag
        decodeTag()
        if (!(reader.peek() hasMajor major)) throw CborDecoderException.Default

        isStructureInfinite = (reader.peek() and SIZE_INFINITE) == SIZE_INFINITE
        if (!isStructureInfinite) size = decodeCollectionSize(descriptor)
        else reader.consume()
        beginDone = true
        return this
    }

    /** decode element in order by default */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = when {
        isStructureInfinite && reader.peek() == HEADER_BREAK -> CompositeDecoder.DECODE_DONE
        indexCounter == size -> CompositeDecoder.DECODE_DONE
        else -> indexCounter.also { indexCounter += 1 }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (endDone) return super.endStructure(descriptor)
        endDone = true
        if (isStructureInfinite && reader.peekConsume() != HEADER_BREAK) throw CborDecoderException.Default
    }
}

