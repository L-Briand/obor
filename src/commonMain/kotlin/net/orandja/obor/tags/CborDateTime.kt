package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import kotlin.jvm.JvmInline

@CborTag(0, true)
@Serializable
@JvmInline
value class CborDateTime(val dateTime: String) {
    override fun toString(): String = dateTime
}