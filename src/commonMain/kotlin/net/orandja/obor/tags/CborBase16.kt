package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.serializer.CborByteArraySerializer
import kotlin.jvm.JvmInline

@CborTag(23, true)
@Serializable
@JvmInline
value class CborBase16(@Serializable(CborByteArraySerializer::class) val bytes: ByteArray) {
    @OptIn(ExperimentalStdlibApi::class)
    fun decoded() = bytes.toHexString()
}