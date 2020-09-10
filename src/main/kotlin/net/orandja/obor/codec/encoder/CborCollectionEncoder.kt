package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal abstract class CborCollectionEncoder(
    out: CborWriter,
    serializersModule: SerializersModule,
    override var chunkSize: Int,
) : CborEncoder(out, serializersModule) {
    private var endMarker: Boolean = false
    private var beginDone: Boolean = false
    private var endDone: Boolean = false

    protected var isFinite: Boolean = false
    abstract val finiteToken: UByte
    abstract val infiniteToken: UByte

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (beginDone) return super.beginCollection(descriptor, collectionSize)
        val chunkSize = (descriptor.annotations.find { it is CborInfinite } as? CborInfinite)?.chunkSize ?: chunkSize
        return if (chunkSize in 0 until collectionSize) {
            beginStructure(descriptor)
        } else {
            out.writeMajor32(finiteToken, collectionSize.toUInt())
            isFinite = true
            beginDone = true
            this
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (beginDone) return super.beginStructure(descriptor)
        out.write(infiniteToken)
        isFinite = false
        endMarker = true
        beginDone = true
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (endMarker && !endDone) {
            out.write(HEADER_BREAK)
            endDone = true
        }
    }
}