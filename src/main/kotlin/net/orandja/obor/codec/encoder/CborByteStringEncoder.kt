package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BYTE_INFINITE
import net.orandja.obor.codec.HEADER_BYTE_START
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.codec.writer.CborWriter
import net.orandja.obor.vector.UByteVector

/**
 * Encoder for Byte String.
 *
 * It has 2 behavior :
 *  - Finite: Write the list with a fixed size and bypass header for each byte
 *  - Infinite: Buffer each chunk before pushing it through the [writer]
 *
 * @see CborWriter
 */
@ExperimentalSerializationApi
@ExperimentalUnsignedTypes
@InternalSerializationApi
internal class CborByteStringEncoder(writer: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(writer, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_BYTE_START
    override val infiniteToken: UByte = HEADER_BYTE_INFINITE

    /** Index of the current element to encode. Only useful when byteString is infinite. */
    private var index: Int = 0

    /** Buffer for infinite string. */
    private val buffer by lazy { UByteVector(chunkSize) }

    override fun encodeByte(value: Byte) {
        updateIndex()
        if (isFinite) writer.write(value.toUByte())
        else buffer.add(value.toUByte())
    }

    private fun updateIndex() {
        if (isFinite) return
        if (index == chunkSize) flush()
        index += 1
    }

    private fun flush() {
        if (isFinite && index < 0) return
        index = 0
        writer.writeMajor32(MAJOR_BYTE, buffer.size.toUInt())
        writer.write(buffer.nativeArray)
        buffer.clear()
    }

    override fun endStructure(descriptor: SerialDescriptor) = flush().also { super.endStructure(descriptor) }

    // TODO: Restrict for bytes only
}