package net.orandja.obor

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.orandja.obor.codec.Cbor
import net.orandja.obor.codec.HEADER_BREAK
import net.orandja.obor.codec.SIZE_INFINITE
import net.orandja.obor.io.ByteVector
import net.orandja.obor.io.CborByteWriter
import net.orandja.obor.io.CborWriter
import kotlin.math.ceil
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


fun buildSize(major: UByte, amount: Int, value: UByte = 0u) = buildCbor { buildSize(major, amount, value) }
fun CborWriter.buildSize(major: UByte, amount: Int, value: UByte = 0u) {
    writeMajor32(major, (amount).toUInt())
    repeat(amount) { write(value) }
}

fun buildChunkedInfinite(major: UByte, amount: Int, chunk: Int, value: UByte = 0u) = buildCbor {
    write((major or SIZE_INFINITE).toUByte())
    repeat(amount / chunk) { buildSize(major, chunk, value) }
    if (amount % chunk > 0) buildSize(major, amount % chunk, value)
    write(HEADER_BREAK)
}

fun buildInfinite(major: UByte, amount: Int, onValue: CborWriter.() -> Unit) = buildCbor {
    write((major or SIZE_INFINITE).toUByte())
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
    val result = ByteVector()
    CborByteWriter(result).apply(writer)
    return result.nativeArray
}


inline fun <reified T> assertTransformation(expected: ByteArray, data: T, serializer: KSerializer<T> = serializer()) {
    assertEquals(data, Cbor.decodeFromByteArray(serializer, expected))
    assertContentEquals(expected, Cbor.encodeToByteArray(serializer, data))
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Int.chunkedSize(chunkSize: Int) = ceil(toFloat() / chunkSize.toFloat()).toInt()

@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.chunked(chunkSize: Int): Sequence<ByteArray> = sequence {
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
internal inline fun ByteArray.chunkedArray(chunkSize: Int): Array<ByteArray> {
    val chunks = chunked(chunkSize).iterator()
    return Array(size.chunkedSize(chunkSize)) { chunks.next() }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.chunkedList(chunkSize: Int): List<ByteArray> = chunked(chunkSize).toList()