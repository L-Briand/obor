package net.orandja.obor.io

/** Represent a growing native array */
interface Vector<T, ArrayOfT> {
    val array: ArrayOfT
    val size: Int

    val nativeArray: ArrayOfT

    operator fun get(index: Int): T
    operator fun set(index: Int, value: T)

    fun add(value: T)
    fun add(array: ArrayOfT, offset: Int, count: Int)

    fun clear()
}