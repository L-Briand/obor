package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal object CborEncoderNoOp : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    public override fun encodeValue(value: Any): Unit = Unit

    override fun encodeNull(): Unit = Unit

    override fun encodeBoolean(value: Boolean): Unit = Unit
    override fun encodeByte(value: Byte): Unit = Unit
    override fun encodeShort(value: Short): Unit = Unit
    override fun encodeInt(value: Int): Unit = Unit
    override fun encodeLong(value: Long): Unit = Unit
    override fun encodeFloat(value: Float): Unit = Unit
    override fun encodeDouble(value: Double): Unit = Unit
    override fun encodeChar(value: Char): Unit = Unit
    override fun encodeString(value: String): Unit = Unit
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int): Unit = Unit
}
