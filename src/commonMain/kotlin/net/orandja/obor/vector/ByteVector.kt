package net.orandja.obor.vector

@ExperimentalUnsignedTypes
internal class ByteVector(initialCapacity: Int = 32) : Vector<Byte, ByteArray> {
    override var array: ByteArray = ByteArray(initialCapacity)
        private set
    override var size: Int = 0
        private set

    private fun ensureCapacity(elementsToAppend: Int) {
        if (size + elementsToAppend <= array.size) return
        val newArray = ByteArray((size + elementsToAppend).takeHighestOneBit() shl 1)
        array.copyInto(newArray)
        array = newArray
    }

    override fun get(index: Int): Byte =
        if (index in 0..<size) array[index] else oob("Index $index is out of bounds. Range: ${0..<size} ")

    override fun set(index: Int, value: Byte) =
        if (index in 0..<size) array[index] = value else oob("Index $index is out of bounds. Range: ${0..<size} ")

    override fun add(value: Byte) {
        ensureCapacity(1)
        array[size] = value
        size += 1
    }

    override fun add(array: ByteArray, offset: Int, count: Int) {
        if (count !in 0..array.size || count + offset !in 0..array.size)
            oob("Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset} ")
        if (count == 0) return

        ensureCapacity(count)
        array.copyInto(array, size, offset, offset + count)
        size += count
    }

    override fun clear() {
        size = 0
    }
}