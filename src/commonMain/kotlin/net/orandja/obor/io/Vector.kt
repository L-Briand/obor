package net.orandja.obor.io

/** Represent a growing native array */
interface Vector<T, ArrayOfT> {
    val array: ArrayOfT
    val size: Int
    val nativeArray: ArrayOfT

    fun ensureCapacity(elementsToAppend: Int)
    fun add(value: T)
    fun add(array: ArrayOfT, offset: Int, count: Int)
}