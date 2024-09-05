package net.orandja.obor;

import kotlinx.serialization.Serializable
import net.orandja.obor.annotations.CborIndefinite
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Indefinite<T>(@CborIndefinite val array: T)