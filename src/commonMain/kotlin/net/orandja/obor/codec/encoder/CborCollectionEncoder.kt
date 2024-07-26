package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.writer.CborWriter

/** Generic encoder for all [StructureKind] elements */
@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal abstract class CborCollectionEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    override var chunkSize: Int,
) : CborEncoder(writer, serializersModule) {

    /** beginCollection or beginStructure are done */
    private var beginDone: Boolean = false

    /** endStructure is done */
    private var endDone: Boolean = false

    /** is encoded structure is finite */
    protected var isFinite: Boolean = false
        private set

    /** Major to write at the start of the structure */
    abstract val finiteToken: UByte

    /** Major to write at the end of the structure */
    abstract val infiniteToken: UByte

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (beginDone) return super.beginCollection(descriptor, collectionSize)
        // annotated class take priority over annotated fields
        val chunkSize = (descriptor.annotations.find { it is CborInfinite } as? CborInfinite)?.chunkSize ?: chunkSize

        encodeTag((descriptor.annotations.find { it is CborTag } as? CborTag)?.tag ?: tag)

        return if (chunkSize in 0 until collectionSize) { // number of elements exceed chunk size -> infinite
            beginStructure(descriptor)
        } else {
            writer.writeMajor32(finiteToken, collectionSize.toUInt())
            isFinite = true
            beginDone = true
            this
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (beginDone) return super.beginStructure(descriptor)
        writer.write(infiniteToken)
        isFinite = false
        beginDone = true
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (!isFinite && !endDone) {
            writer.write(HEADER_BREAK)
            endDone = true
        }
    }
}