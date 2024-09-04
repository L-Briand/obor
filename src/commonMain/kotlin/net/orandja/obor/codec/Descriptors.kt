package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import net.orandja.obor.annotations.CborIndefinite

@OptIn(ExperimentalSerializationApi::class)
internal abstract class ListLikeDescriptor(name: String, private val elementDescriptor: SerialDescriptor) :
    SerialDescriptor {
    override val annotations: List<Annotation> = listOf()
    override val elementsCount: Int = 1
    override val kind: SerialKind = StructureKind.LIST
    override val serialName: String = name

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int = name.toInt()
    override fun isElementOptional(index: Int): Boolean = false
    override fun getElementAnnotations(index: Int): List<Annotation> = emptyList()
    override fun getElementDescriptor(index: Int): SerialDescriptor = elementDescriptor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListLikeDescriptor) return false
        if (elementDescriptor == other.elementDescriptor && serialName == other.serialName) return true
        return false
    }

    override fun hashCode(): Int {
        return elementDescriptor.hashCode() * 31 + serialName.hashCode()
    }
}

internal class ByteArrayDescriptor(name: String) : ListLikeDescriptor(name, Byte.serializer().descriptor)
internal class ListBytesDescriptor(name: String) : ListLikeDescriptor(name, ByteArrayDescriptor(name)) {
    override val annotations: List<Annotation> = listOf(CborIndefinite())
}

internal class ListStringsDescriptor(name: String) : ListLikeDescriptor(name, String.serializer().descriptor) {
    override val annotations: List<Annotation> = listOf(CborIndefinite())
}
