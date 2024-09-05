package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.serializer.CborByteArraySerializer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.JvmInline

@CborTag(21, true)
@Serializable
@JvmInline
@OptIn(ExperimentalEncodingApi::class)
value class CborBase64Url(@Serializable(CborByteArraySerializer::class) val bytes: ByteArray) {
    fun decodedValue() = Base64.withPadding(Base64.PaddingOption.ABSENT).decode(bytes)
}