package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotate a class or a property to add the [tag] before it.
 *
 * Set [require] to true to throw an exception if the tag is not present during deserialization.
 */
@SerialInfo
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class CborTag(val tag: Long, val require: Boolean = false)