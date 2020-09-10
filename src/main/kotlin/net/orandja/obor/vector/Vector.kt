package net.orandja.obor.vector

internal interface Vector<T> {
    operator fun get(index: Int): T
    operator fun set(index: Int, value: T)
    val size: Int
    fun add(value: T)
    fun add(array: Array<T>, offset: Int = 0, count: Int = array.size)
    val array: Array<T>
}