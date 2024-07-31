package net.orandja.obor;

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborInfinite
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Infinite<T>(@CborInfinite val array: T)