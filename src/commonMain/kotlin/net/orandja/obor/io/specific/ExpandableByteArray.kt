package net.orandja.obor.io.specific

import net.orandja.obor.io.ByteWriter
import net.orandja.obor.io.WriterException

/**
 * Represent a growing ByteArray,
 * [ByteWriter]'s methods are meant to add elements without worries.
 * One implementation can access the [array] directly for better performance, but should keep track of the [size] itself.
 */
class ExpandableByteArray(initialCapacity: Int = 64) : ByteWriter {
    var array: ByteArray = ByteArray(initialCapacity)
    var size: Int = 0
    fun getSizedArray(): ByteArray = array.copyOfRange(0, size)

    fun ensureCapacity(elementsToAppend: Int) {
        if (size + elementsToAppend <= array.size) return
        val newArray = ByteArray((size + elementsToAppend).takeHighestOneBit() shl 1)
        array.copyInto(newArray)
        array = newArray
    }

    override fun totalWrite(): Long = size.toLong()

    override fun write(value: Byte) {
        ensureCapacity(1)
        array[size] = value
        size += 1
    }

    override fun write(array: ByteArray, offset: Int, count: Int) {
        if (count == 0) return
        if (count !in 0..array.size || count + offset !in 0..array.size) throw WriterException(
            "Requested array range is out of range. Range is ${0..array.size} Requested is ${count..<count + offset}"
        )

        ensureCapacity(count)
        array.copyInto(this.array, size, offset, offset + count)
        size += count
    }

}