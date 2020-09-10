package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.MAJOR_BYTE
import net.orandja.obor.codec.hasFlags
import net.orandja.obor.codec.reader.CborReader

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborByteStringDecoder(
    input: CborReader,
    serializersModule: SerializersModule
) : CborCollectionDecoder(input, serializersModule) {
    override val major: UByte = MAJOR_BYTE

    private var innerCollectionSize = 0
    private var indexCounter = -1

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (isInfinite && input.peek() != HEADER_BREAK && input.peek() hasFlags major && innerCollectionSize == 0)
            innerCollectionSize = decodeCollectionSize(descriptor)
        return super.decodeElementIndex(descriptor)
    }

    override fun decodeByte(): Byte {
        indexCounter += 1
        if (indexCounter == innerCollectionSize) {
            indexCounter = -1
            innerCollectionSize = 0
        }
        return input.peekConsume().toByte()
    }

}