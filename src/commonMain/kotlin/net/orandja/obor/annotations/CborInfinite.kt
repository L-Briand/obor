package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotate an element to serialize it with infinite mark.
 * Can be applied to any structure kind: Object, Class, List, Map, Array, etc.
 *
 * This annotation has no effect on deserialization.
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class CborInfinite