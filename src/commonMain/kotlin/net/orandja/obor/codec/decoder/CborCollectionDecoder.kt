package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.SIZE_INFINITE
import net.orandja.obor.codec.hasMajor
import net.orandja.obor.codec.newDecoderTracker
import net.orandja.obor.io.CborReader

/** Base class for decoding all [StructureKind] */
@OptIn(ExperimentalSerializationApi::class)
internal abstract class CborCollectionDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborDecoder(reader, serializersModule, newDecoderTracker(parent)) {

    protected var isStructureInfinite: Boolean = false

    /** Used to know the number of elements currently decoded */
    private var indexCounter = 0

    /** The Size of elements inside this structure (-1 is infinite) */
    private var size = -1

    /**
     * Major kind of the structure expected to decode.
     * Throw an exception if it does not match the expected type
     */
    abstract val major: UByte

    override fun startStructure(descriptor: SerialDescriptor): CompositeDecoder {
        super.startStructure(descriptor)
        if (!(reader.peek() hasMajor major))
            throw CborDecoderException.Default()

        isStructureInfinite = (reader.peek() and SIZE_INFINITE) == SIZE_INFINITE
        if (!isStructureInfinite) size = decodeCollectionSize(descriptor)
        else reader.consume()
        return this
    }

    /** decode element in order by default */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = when {
        isStructureInfinite && reader.peek() == HEADER_BREAK -> CompositeDecoder.DECODE_DONE
        indexCounter == size -> CompositeDecoder.DECODE_DONE
        else -> {
            val index = indexCounter++
            if(index < descriptor.elementsCount) readTag(descriptor.getElementAnnotations(index))
            index
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (isStructureInfinite && reader.peekConsume() != HEADER_BREAK)
            throw CborDecoderException.Default()
        return super.endStructure(descriptor)
    }
}

