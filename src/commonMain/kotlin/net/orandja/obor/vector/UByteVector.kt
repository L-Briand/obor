package net.orandja.obor.vector

@ExperimentalUnsignedTypes
internal class UByteVector(initialCapacity: Int = 32) : Vector<UByte, UByteArray> {
    override var array: UByteArray = UByteArray(initialCapacity)
        private set
    override var size: Int = 0
        private set

    val nativeArray get() = array.copyOfRange(0, size)

    private fun ensureCapacity(elementsToAppend: Int) {
        if (size + elementsToAppend <= array.size) return
        val newArray = UByteArray((size + elementsToAppend).takeHighestOneBit() shl 1)
        array.copyInto(newArray)
        array = newArray
    }

    override fun get(index: Int): UByte =
        if (index in 0..<size) array[index] else oob("Index $index is out of bounds. Range: ${0..<size} ")

    override fun set(index: Int, value: UByte) =
        if (index in 0..<size) array[index] = value else oob("Index $index is out of bounds. Range: ${0..<size} ")

    override fun add(value: UByte) {
        ensureCapacity(1)
        array[size] = value
        size += 1
    }

    override fun add(array: UByteArray, offset: Int, count: Int) {
        if (count !in 0..array.size || count + offset !in 0..array.size)
            oob("Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset} ")
        if (count == 0) return

        ensureCapacity(count)
        array.copyInto(this.array, size, offset, offset + count)
        size += count
    }

    override fun clear() {
        size = 0
    }
}