package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.SIZE_INFINITE
import net.orandja.obor.codec.hasFlags
import net.orandja.obor.codec.reader.CborReader

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal abstract class CborCollectionDecoder(
    input: CborReader,
    serializersModule: SerializersModule
) : CborDecoder(input, serializersModule) {
    protected var isInfinite: Boolean = false

    private var beginDone = false
    private var endDone = false

    private var indexCounter = 0
    private var size = -1

    abstract val major: UByte

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (beginDone) return super.beginStructure(descriptor)
        if (!(input.peek() hasFlags major)) throw CborDecoderException.Default

        isInfinite = (input.peek() and SIZE_INFINITE) == SIZE_INFINITE
        if (!isInfinite) size = decodeCollectionSize(descriptor)
        else input.consume()
        beginDone = true
        return this
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = when {
        isInfinite && input.peek() == HEADER_BREAK -> CompositeDecoder.DECODE_DONE
        indexCounter == size -> CompositeDecoder.DECODE_DONE
        else -> indexCounter.also { indexCounter += 1 }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (endDone) return super.endStructure(descriptor)
        endDone = true
        if (isInfinite && input.peekConsume() != HEADER_BREAK) throw CborDecoderException.Default
    }
}

