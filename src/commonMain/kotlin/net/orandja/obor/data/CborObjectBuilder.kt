package net.orandja.obor.data

import kotlin.experimental.xor

abstract class CborObjectBuilder internal constructor() {

    // PRIMITIVE

    fun value(value: Byte) = if (value < 0) negative(value.xor(-1)) else positive(value)
    fun value(value: Short) = if (value < 0) negative(value.xor(-1)) else positive(value)
    fun value(value: Int) = if (value < 0) negative(value.xor(-1)) else positive(value)
    fun value(value: Long) = if (value < 0) negative(value.xor(-1)) else positive(value)
    fun value(value: Float) = value(value.toDouble())
    fun value(value: Double) = CborFloat(value)
    fun value(value: Boolean) = CborBoolean(value)
    fun value(value: String) = CborText(value)
    fun value(value: ByteArray) = CborBytes(value)

    val nullElement get() = CborNull
    val undefined get() = CborUndefined

    fun positive(value: Byte) = positive(value.toUByte())
    fun positive(value: Short) = positive(value.toUShort())
    fun positive(value: Int) = positive(value.toUInt())
    fun positive(value: Long) = positive(value.toULong())

    fun positive(value: UByte) = CborPositive(value.toULong())
    fun positive(value: UShort) = CborPositive(value.toULong())
    fun positive(value: UInt) = CborPositive(value.toULong())
    fun positive(value: ULong) = CborPositive(value)

    fun negative(value: Byte) = negative(value.toUByte())
    fun negative(value: Short) = negative(value.toUShort())
    fun negative(value: Int) = negative(value.toUInt())
    fun negative(value: Long) = negative(value.toULong())

    fun negative(value: UByte) = CborNegative(value.toULong())
    fun negative(value: UShort) = CborNegative(value.toULong())
    fun negative(value: UInt) = CborNegative(value.toULong())
    fun negative(value: ULong) = CborNegative(value)

    fun tag(tag: Long, value: CborObject) = CborTagged(tag, value)

    // INDEFINITE

    fun indefiniteText(vararg value: String) = indefiniteText(value.toList())
    fun indefiniteText(value: List<String>): CborTextIndefinite =
        CborTextIndefinite(value.mutate(::CborText))

    fun buildIndefiniteText(builder: MutableList<String>.() -> Unit): CborTextIndefinite {
        val data = mutableListOf<String>().apply(builder)
        return CborTextIndefinite(data.mutate(::CborText))
    }

    fun indefiniteBytes(vararg value: ByteArray) = indefiniteBytes(value.toList())
    fun indefiniteBytes(value: List<ByteArray>) = CborBytesIndefinite(value.mutate(::CborBytes))

    fun buildIndefiniteBytes(builder: MutableList<ByteArray>.() -> Unit): CborBytesIndefinite {
        val data = mutableListOf<ByteArray>().apply(builder)
        return CborBytesIndefinite(data.mutate(::CborBytes))
    }

    // ARRAY

    class CborArrayBuilder internal constructor(
        var isIndefinite: Boolean = false,
        private val elements: MutableList<CborObject> = mutableListOf()
    ) : CborObjectBuilder(), MutableList<CborObject> by elements {
        override fun build(): CborArray = array(elements, isIndefinite)
    }

    fun array(vararg value: CborObject, indefinite: Boolean = false) = array(value.toList(), indefinite)
    fun array(value: List<CborObject>, indefinite: Boolean = false) =
        CborArray(MutableList(value.size) { value[it] }, indefinite)

    fun buildArray(indefinite: Boolean = false, builder: CborArrayBuilder.() -> Unit): CborArray =
        CborArrayBuilder(indefinite).apply(builder).build()

    // MAP

    class CborMapBuilder internal constructor(
        var isIndefinite: Boolean = false, private val elements: MutableMap<CborObject, CborObject> = mutableMapOf()
    ) : CborObjectBuilder(), MutableMap<CborObject, CborObject> by elements {
        override fun build(): CborMap = map(elements, isIndefinite)
    }

    fun map(vararg value: Pair<CborObject, CborObject>, indefinite: Boolean = false): CborMap =
        map(value.toMap(), indefinite)

    fun map(value: Map<CborObject, CborObject>, indefinite: Boolean = false): CborMap {
        val iterator = value.iterator()
        val data = MutableList(value.size) {
            val (key, value) = iterator.next()
            CborMapEntry(key, value)
        }
        return CborMap(data, indefinite)
    }


    fun buildMap(indefinite: Boolean = false, builder: CborMapBuilder.() -> Unit): CborMap =
        CborMapBuilder(indefinite).apply(builder).build()

    // ORDERED MAP

    class CborOrderedMapBuilder internal constructor(
        var isIndefinite: Boolean = false,
        private val elements: MutableList<CborMapEntry> = mutableListOf()
    ) : CborObjectBuilder(), MutableList<CborMapEntry> by elements {
        override fun build(): CborMap = orderedMap(elements, isIndefinite)
    }

    fun orderedMap(vararg value: CborMapEntry, indefinite: Boolean = false): CborMap =
        orderedMap(value.toMutableList(), indefinite)

    fun orderedMap(vararg value: Pair<CborObject, CborObject>, indefinite: Boolean = false): CborMap =
        orderedMap(value.map { (key, value) -> CborMapEntry(key, value) }, indefinite)

    fun orderedMap(value: List<CborMapEntry>, indefinite: Boolean = false): CborMap {
        val data = if (value is MutableList<CborMapEntry>) value else MutableList(value.size) { value[it] }
        return CborMap(data, indefinite)
    }

    fun buildOrderedMap(indefinite: Boolean = false, builder: CborOrderedMapBuilder.() -> Unit): CborMap =
        CborOrderedMapBuilder(indefinite).apply(builder).build()

    // TOOLS

    private inline fun <reified T, R> List<T>.mutate(transform: T.() -> R): MutableList<R> =
        MutableList(size) { transform(this[it]) }

    internal open fun build(): CborObject = undefined
}