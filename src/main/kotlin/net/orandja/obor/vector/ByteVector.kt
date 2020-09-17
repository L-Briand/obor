package net.orandja.obor.vector

@ExperimentalUnsignedTypes
internal class ByteVector(initialCapacity: Int = 32) : Vector<Byte> {
    private var _array: ByteArray = ByteArray(initialCapacity)
    private var _size: Int = 0

    override val size: Int get() = _size
    override val array: Array<Byte> = _array.sliceArray(0 until size).toTypedArray()
    val nativeArray: ByteArray get() = _array.sliceArray(0 until size)

    private fun ensureCapacity(elementsToAppend: Int) {
        if (_size + elementsToAppend <= _array.size) return
        val newArray = ByteArray((_size + elementsToAppend).takeHighestOneBit() shl 1)
        _array.copyInto(newArray)
        _array = newArray
    }

    override fun get(index: Int): Byte = if (index in 0 until size) _array[index] else throw IndexOutOfBoundsException()
    override fun set(index: Int, value: Byte) = if (index in 0 until size) _array[index] = value else throw IndexOutOfBoundsException()

    override fun add(value: Byte) {
        ensureCapacity(1)
        _array[_size] = value
        _size += 1
    }

    override fun add(array: Array<Byte>, offset: Int, count: Int) = add(array.toByteArray(), offset, count)
    fun add(array: ByteArray, offset: Int = 0, count: Int = array.size) {
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