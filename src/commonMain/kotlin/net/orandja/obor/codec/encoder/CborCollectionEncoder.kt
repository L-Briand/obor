package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborSkip
import net.orandja.obor.codec.*
import net.orandja.obor.io.CborWriter

/** Generic encoder for all [StructureKind] elements */
@OptIn(ExperimentalSerializationApi::class)
internal abstract class CborCollectionEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborEncoder(writer, serializersModule, newEncoderTracker(parent)) {

    /** Major to write at the start of the structure */
    abstract val finiteToken: UByte

    /** Major to write at the end of the structure */
    abstract val infiniteToken: UByte

    override fun startCollection(descriptor: SerialDescriptor, collectionSize: Int): CborEncoder {
        super.startCollection(descriptor, collectionSize)

        if (tracker.encFieldIsInfinite || tracker.encClassIsInfinite) {
            writer.write(infiniteToken)
            return this
        }

        var newCollectionSize = collectionSize
        for (i in 0 until descriptor.elementsCount) {
            if (descriptor.getElementAnnotations(i).any { it is CborSkip }) newCollectionSize--
        }
        writer.writeMajor32(finiteToken, newCollectionSize.toUInt())
        return this
    }

    override fun startStructure(descriptor: SerialDescriptor): CborEncoder {
        super.startStructure(descriptor)
        writer.write(infiniteToken)
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (tracker.encParentIsInfinite || tracker.encClassIsInfinite) writer.write(HEADER_BREAK)
    }
}