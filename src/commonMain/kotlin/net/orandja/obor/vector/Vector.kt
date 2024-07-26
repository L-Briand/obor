package net.orandja.obor.vector

/** Represent a growing native array */
internal interface Vector<T, ArrayOfT> {
    val array: ArrayOfT
    val size: Int

    operator fun get(index: Int): T
    operator fun set(index: Int, value: T)

    fun add(value: T)
    fun add(array: ArrayOfT, offset: Int = 0, count: Int)

    fun clear()
}