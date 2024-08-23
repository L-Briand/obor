package net.orandja.obor

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.SIZE_INFINITE
import net.orandja.obor.io.ExpandableByteArray
import net.orandja.obor.io.CborWriterExpandableByteArray
import net.orandja.obor.io.CborWriter
import kotlin.experimental.or
import kotlin.math.ceil
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


fun buildSize(major: Byte, amount: Int, value: Byte = 0) = buildCbor { buildSize(major, amount, value) }
fun CborWriter.buildSize(major: Byte, amount: Int, value: Byte = 0) {
    writeMajor32(major, amount)
    repeat(amount) { write(value) }
}

fun buildChunkedInfinite(major: Byte, amount: Int, chunk: Int, value: Byte = 0) = buildCbor {
    write((major or SIZE_INFINITE))
    repeat(amount / chunk) { buildSize(major, chunk, value) }
    if (amount % chunk > 0) buildSize(major, amount % chunk, value)
    write(HEADER_BREAK)
}

fun buildInfinite(major: Byte, amount: Int, onValue: CborWriter.() -> Unit) = buildCbor {
    write((major or SIZE_INFINITE))
    repeat(amount) { onValue() }
    write(HEADER_BREAK)
}

@OptIn(ExperimentalStdlibApi::class)
fun String.hex(): ByteArray = this.hexToByteArray(HexFormat.UpperCase)

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.hex(): String = toHexString(HexFormat.UpperCase)

inline infix fun <reified T> ByteArray.decodeCbor(serializer: KSerializer<T>) =
    Cbor.decodeFromByteArray(serializer, this)

inline infix fun <reified T> T.encodeCbor(serializer: KSerializer<T>) =
    Cbor.encodeToByteArray(serializer, this)

internal fun buildCbor(writer: CborWriter.() -> Unit): ByteArray {
    val result = ExpandableByteArray()
    CborWriterExpandableByteArray(result).apply(writer)
    return result.getSizedArray()
}


inline fun <reified T> assertTransformation(expected: ByteArray, data: T, serializer: KSerializer<T> = serializer()) {
    assertEquals(data, Cbor.decodeFromByteArray(serializer, expected))
    assertContentEquals(expected, Cbor.encodeToByteArray(serializer, data))
}

internal fun Int.chunkedSize(chunkSize: Int) = ceil(toFloat() / chunkSize.toFloat()).toInt()

internal fun ByteArray.chunked(chunkSize: Int): Sequence<ByteArray> = sequence {
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

internal fun ByteArray.chunkedArray(chunkSize: Int): Array<ByteArray> {
    val chunks = chunked(chunkSize).iterator()
    return Array(size.chunkedSize(chunkSize)) { chunks.next() }
}

internal fun ByteArray.chunkedList(chunkSize: Int): List<ByteArray> = chunked(chunkSize).toList()