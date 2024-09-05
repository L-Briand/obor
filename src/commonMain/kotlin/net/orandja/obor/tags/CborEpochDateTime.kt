package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import kotlin.jvm.JvmInline

@CborTag(1, true)
@Serializable
@JvmInline
value class CborEpochDateTime(val epoch: Long) {
    override fun toString(): String = epoch.toString()
}