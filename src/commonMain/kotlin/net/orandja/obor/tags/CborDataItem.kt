package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.serializer.CborByteArraySerializer
import kotlin.jvm.JvmInline

@CborTag(24, true)
@Serializable
@JvmInline
value class CborDataItem(@Serializable(CborByteArraySerializer::class) val bytes: ByteArray)