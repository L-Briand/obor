package net.orandja.obor.io

/**
 * Represent a growing native array,
 * One implementation can access the array directly for better performance, but should keep track of the size itself.
 */
interface ExpandableArray<T, arrayOfT> : Writer<T, arrayOfT> {
    val array: arrayOfT
    val size: Int
    fun getSizedArray(): arrayOfT
    fun ensureCapacity(elementsToAppend: Int)
}