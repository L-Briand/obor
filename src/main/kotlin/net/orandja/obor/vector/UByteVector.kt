package net.orandja.obor.vector

@ExperimentalUnsignedTypes
internal class UByteVector(initialCapacity: Int = 32) : Vector<UByte> {
    private var _array: UByteArray = UByteArray(initialCapacity)
    private var _size: Int = 0

    override val size: Int get() = _size
    override val array: Array<UByte> = _array.sliceArray(0 until size).toTypedArray()
    val nativeArray: UByteArray get() = _array.sliceArray(0 until size)

    private fun ensureCapacity(elementsToAppend: Int) {
        if (_size + elementsToAppend <= _array.size) return
        val newArray = UByteArray((_size + elementsToAppend).takeHighestOneBit() shl 1)
        _array.copyInto(newArray)
        _array = newArray
    }

    override fun get(index: Int): UByte = if (index in 0 until size) _array[index] else throw IndexOutOfBoundsException()
    override fun set(index: Int, value: UByte) = if (index in 0 until size) _array[index] = value else throw IndexOutOfBoundsException()

    override fun add(value: UByte) {
        ensureCapacity(1)
        _array[_size] = value
        _size += 1
    }

    override fun add(array: Array<UByte>, offset: Int, count: Int) = add(array.toUByteArray(), offset, count)
    fun add(array: UByteArray, offset: Int = 0, count: Int = array.size) {
        if (count !in 0..array.size || count + offset !in 0..array.size) throw IndexOutOfBoundsException()
        if (count == 0) return

        ensureCapacity(count)
        array.copyInto(_array, _size, offset, offset + count)
        _size += count
    }

    override fun clear() {
        _size = 0
    }
}