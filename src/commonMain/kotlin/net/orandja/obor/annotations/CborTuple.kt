package net.orandja.obor.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotate a Class or object to serialize it as an ordered list.
 * Instead of writing `{ foo: "a", bar: 0 }` it writes `["a", 0]`.
 *
 * @param inlinedInList If the class is the sole element of a list like structure
 *        (Ex: `List<T>` where `T` is `@CborTuple(true) class T`),
 *        the list flattens T and becomes repeating elements of T.
 *        (Ex: `[[a, b], [a, b]]` becomes `[a, b, a, b]`)
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS)
annotation class CborTuple(val inlinedInList: Boolean = false)