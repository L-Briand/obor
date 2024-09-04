package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Indicates to always use indefinite encoding when encoding the annotated value.
 * Can be applied to any structure kind: Object, Class, List, Map, Array, etc.
 *
 * This annotation has no effect on deserialization.
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class CborIndefinite
