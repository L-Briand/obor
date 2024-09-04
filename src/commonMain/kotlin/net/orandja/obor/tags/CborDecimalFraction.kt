package net.orandja.obor.tags

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.annotations.CborTuple

@CborTag(4, true)
@Serializable
@CborTuple
data class CborDecimalFraction(val exponent: Long, val mantissa: Long)