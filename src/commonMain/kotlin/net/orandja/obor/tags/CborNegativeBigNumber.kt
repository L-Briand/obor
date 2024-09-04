package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.serializer.CborByteArraySerializer
import kotlin.jvm.JvmInline

@CborTag(3, true)
@Serializable
@JvmInline
value class CborNegativeBigNumber(@Serializable(CborByteArraySerializer::class) val number: ByteArray) {

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = number.toHexString(HexFormat.Default)
}