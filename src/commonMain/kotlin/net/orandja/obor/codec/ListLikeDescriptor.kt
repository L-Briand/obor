package net.orandja.obor.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import net.orandja.obor.annotations.CborInfinite

/**
 * Class content copied from [kotlinx.serialization.internal.ListLikeDescriptor]
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
@OptIn(ExperimentalSerializationApi::class)
abstract class ListLikeDescriptor(name: String, private val elementDescriptor: SerialDescriptor) : SerialDescriptor {
    override val elementsCount: Int = 1
    override val kind: SerialKind = StructureKind.LIST
    override val serialName: String = name

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun isElementOptional(index: Int): Boolean {
        require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
        return elementDescriptor
    }

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

class ByteArrayDescriptor(name: String) : ListLikeDescriptor(name, Byte.serializer().descriptor)
class ListBytesDescriptor(name: String) : ListLikeDescriptor(name, ByteArrayDescriptor(name)) {
    @OptIn(ExperimentalSerializationApi::class)
    override val annotations: List<Annotation> = listOf(CborInfinite())
}

class ListStringsDescriptor(name: String) : ListLikeDescriptor(name, String.serializer().descriptor) {
    @OptIn(ExperimentalSerializationApi::class)
    override val annotations: List<Annotation> = listOf(CborInfinite())
}
