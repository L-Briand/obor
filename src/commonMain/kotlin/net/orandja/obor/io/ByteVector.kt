package net.orandja.obor.io

class ByteVector(initialCapacity: Int = 32) : Vector<Byte, ByteArray> {
    private fun oob(message: String): Nothing = throw IndexOutOfBoundsException(message)

    override var array: ByteArray = ByteArray(initialCapacity)
    override var size: Int = 0

    override val nativeArray get() = array.copyOfRange(0, size)

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
        if (count == 0) return
        if (count !in 0..array.size || count + offset !in 0..array.size)
            oob("Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset}")

        ensureCapacity(count)
        array.copyInto(this.array, size, offset, offset + count)
        size += count
    }

    override fun clear() {
        size = 0
    }
}