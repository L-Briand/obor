package net.orandja.obor

import kotlinx.serialization.Serializable
import net.orandja.obor.serializer.CborByteArraySerializer
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class CborBytes(@Serializable(CborByteArraySerializer::class) val bytes: ByteArray) {

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = bytes.toHexString(HexFormat.Default)
}