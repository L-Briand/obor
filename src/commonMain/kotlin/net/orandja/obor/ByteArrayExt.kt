package net.orandja.obor

import kotlin.math.ceil

/** @return The length of the array if [ByteArray] where to be chunked with [chunkedSize]*/
@Suppress("NOTHING_TO_INLINE")
inline fun Int.chunkedSize(chunkSize: Int) = ceil(toFloat() / chunkSize.toFloat()).toInt()

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.chunked(chunkSize: Int): Sequence<ByteArray> = sequence {
    var chunkSize = chunkSize
    var index = 0
    var offset: Int
    while (true) {
        offset = index * chunkSize
        if (offset + chunkSize >= size) break
        yield(copyOfRange(offset, offset + chunkSize))
        index += 1
    }
    chunkSize = size % chunkSize
    if (chunkSize > 0) yield(copyOfRange(offset, offset + chunkSize))
}

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.chunkedArray(chunkSize: Int): Array<ByteArray> {
    val chunks = chunked(chunkSize).iterator()
    return Array(size.chunkedSize(chunkSize)) { chunks.next() }
}

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.chunkedList(chunkSize: Int): List<ByteArray> = chunked(chunkSize).toList()