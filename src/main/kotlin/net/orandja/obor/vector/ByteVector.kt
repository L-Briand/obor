package net.orandja.obor.vector

internal class ByteVector : Vector<Byte> {
    private var _array: ByteArray = ByteArray(32)
    private var _size: Int = 0

    override val size: Int = _size
    override val array: Array<Byte> = _array.sliceArray(0 until size).toTypedArray()
    val nativeArray: ByteArray = _array.sliceArray(0 until size)

    private fun ensureCapacity(elementsToAppend: Int) {
        if (_size + elementsToAppend <= _array.size) return
        val newArray = ByteArray((_size + elementsToAppend).takeHighestOneBit() shl 1)
        _array.copyInto(newArray)
        _array = newArray
    }

    override fun get(index: Int): Byte = if (index < size) _array[index] else throw IndexOutOfBoundsException()
    override fun set(index: Int, value: Byte) = if (index < size) _array[index] = value else throw IndexOutOfBoundsException()

    override fun add(value: Byte) {
        ensureCapacity(1)
        array[_size++] = value
    }

    override fun add(array: Array<Byte>, offset: Int, count: Int) = add(array.toByteArray(), offset, count)
    fun add(array: ByteArray, offset: Int, count: Int) {
        if (offset !in array.indices || count + offset !in array.indices)
            throw IndexOutOfBoundsException()
        if (count == 0) return

        ensureCapacity(count)
        array.copyInto(_array, _size, offset, offset + count)
        _size += count
    }
}


