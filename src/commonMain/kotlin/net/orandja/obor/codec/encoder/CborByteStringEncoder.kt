package net.orandja.obor.codec.encoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.*
import net.orandja.obor.io.ByteVector
import net.orandja.obor.io.CborWriter

internal class CborByteStringEncoder(
    writer: CborWriter,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionEncoder(writer, serializersModule, parent) {
    override val finiteToken: UByte = HEADER_BYTE_START
    override val infiniteToken: UByte = HEADER_BYTE_INFINITE

    private val buffer by lazy { ByteVector(255) }

    // Elements are all bytes with no annotation.
    // We don't want them to override isFinite field by not having CborInfinite annotation
    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean = true

    override fun encodeByte(value: Byte) {
        if (!(tracker.encParentIsInfinite || tracker.encClassIsInfinite)) writer.write(value.toUByte())
        else {
            buffer.add(value)
            if (buffer.size == 255) flush()
        }
    }

    private fun flush() {
        if (!(tracker.encParentIsInfinite || tracker.encClassIsInfinite) || buffer.size == 0) return
        writer.writeMajor32(MAJOR_BYTE, buffer.size.toUInt())
        writer.write(buffer.nativeArray, 0, buffer.size)
        buffer.clear()
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        flush()
        super.endStructure(descriptor)
    }
}