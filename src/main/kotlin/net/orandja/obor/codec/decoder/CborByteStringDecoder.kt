package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.codec.hasFlags
import net.orandja.obor.codec.reader.CborReader

/** A special decoder for Major 2 (BYTES) */
@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborByteStringDecoder(
    reader: CborReader,
    serializersModule: SerializersModule
) : CborCollectionDecoder(reader, serializersModule) {
    override val major: UByte = MAJOR_BYTE

    private var innerCollectionSize = 0
    private var indexCounter = -1

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        // The peeked value is a subarray of bytes because byte string is infinite.
        if (isStructureInfinite && reader.peek() != HEADER_BREAK && reader.peek() hasFlags major && innerCollectionSize == 0)
            innerCollectionSize = decodeCollectionSize(descriptor)
        return super.decodeElementIndex(descriptor)
    }

    override fun decodeByte(): Byte {
        // update index for decodeElementIndex
        indexCounter += 1
        if (indexCounter == innerCollectionSize) {
            indexCounter = -1
            innerCollectionSize = 0
        }
        return reader.peekConsume().toByte()
    }

    // TODO : Restrict elements to be only byte
}