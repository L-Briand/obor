package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.annotations.CborTuple

@CborTag(5, true)
@Serializable
@CborTuple
data class CborBigFloat(val exponent: Long, val mantissa: Long)