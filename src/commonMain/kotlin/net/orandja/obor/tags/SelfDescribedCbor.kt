package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import kotlin.jvm.JvmInline

@CborTag(55799, true)
@Serializable
@JvmInline
value class SelfDescribedCbor<T>(val value: T)