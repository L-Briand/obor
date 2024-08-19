package net.orandja.obor.io

class ExpandableByteArray(initialCapacity: Int = 64) : ExpandableArray<Byte, ByteArray> {
    override var array: ByteArray = ByteArray(initialCapacity)
    override var size: Int = 0
    override fun getSizedArray(): ByteArray = array.copyOfRange(0, size)

    override fun ensureCapacity(elementsToAppend: Int) {
        if (size + elementsToAppend <= array.size) return
        val newArray = ByteArray((size + elementsToAppend).takeHighestOneBit() shl 1)
        array.copyInto(newArray)
        array = newArray
    }

    override fun write(value: Byte) {
        ensureCapacity(1)
        array[size] = value
        size += 1
    }

    override fun write(array: ByteArray, offset: Int, count: Int) {
        if (count == 0) return
        if (count !in 0..array.size || count + offset !in 0..array.size) throw IndexOutOfBoundsException(
            "Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset}"
        )

        ensureCapacity(count)
        array.copyInto(this.array, size, offset, offset + count)
        size += count
    }
}