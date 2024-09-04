@file:OptIn(ExperimentalSerializationApi::class)

package net.orandja.obor.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import net.orandja.obor.codec.CborDecoder
import net.orandja.obor.codec.CborEncoder

/**
 * [CborObject] serializers are using internal methods of [CborEncoder] and [CborDecoder] and not kotlinx-serialization api's.
 * There is no real usefulness of having proper descriptor in this case.
 */
internal class DummyDescriptor(
    override val serialName: String,
    override val elementsCount: Int = 0,
    override val kind: SerialKind = SerialKind.CONTEXTUAL,
) : SerialDescriptor {
    override fun getElementAnnotations(index: Int): List<Annotation> = error("DummyDescriptor")
    override fun getElementDescriptor(index: Int): SerialDescriptor = error("DummyDescriptor")
    override fun getElementIndex(name: String): Int = error("DummyDescriptor")
    override fun getElementName(index: Int): String = error("DummyDescriptor")
    override fun isElementOptional(index: Int): Boolean = error("DummyDescriptor")
}