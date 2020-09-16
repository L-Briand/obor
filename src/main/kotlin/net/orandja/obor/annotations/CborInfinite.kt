package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotate an element to serialize it as infinite.
 * Can be applied to any structure kind : Object - Class - List - Map.
 *
 * `chunkSize` define the number of element before the structure become infinite.
 * `chunkSize` of `0` means its always infinite. A negative value is the same as the default implementation: always try to write the size.
 * - If applied to a class or an object: `chunkSize` correspond to the numbers of fields before the structure is serialize with infinite Mark.
 * - If applied on a Map or a List: `chunkSize` correspond to the number of elements before the list is serialize with infinite Mark.
 * - If applied on a String or a ByteArray with `@CborRawBytes`: `chunksize` correspond to the length in which the element is chunked.
 *
 * @param chunkSize chunk's size of the infinite element. If applied on Class or Object number of elements before it's render as infinite.
 */
@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class CborInfinite(val chunkSize: Int = 0)