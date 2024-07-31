package net.orandja.obor.codec.decoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.codec.hasMajor
import net.orandja.obor.io.CborReader

/** A special decoder for Major 2 (BYTES) */
internal class CborByteStringDecoder(
    reader: CborReader,
    serializersModule: SerializersModule,
    parent: Array<Long>,
) : CborCollectionDecoder(reader, serializersModule, parent) {
    override val major: UByte = MAJOR_BYTE

    private var innerCollectionSize = 0
    private var indexCounter = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        // The peeked value is a subarray of bytes because byte string is infinite.
        if (isStructureInfinite && reader.peek() != HEADER_BREAK && reader.peek() hasMajor major && innerCollectionSize == 0) {
            innerCollectionSize = decodeCollectionSize(descriptor)
        }
        return super.decodeElementIndex(descriptor)
    }

    override fun decodeByte(): Byte {
        indexCounter += 1
        if (indexCounter == innerCollectionSize) {
            indexCounter = 0
            innerCollectionSize = 0
        }
        return reader.peekConsume().toByte()
    }

    // TODO : Restrict elements to be only byte
}