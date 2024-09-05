package net.orandja.obor.io


/** Something that can read in a native array */
interface NativeArrayReader<T, arrayOfT> {
    fun totalRead(): Long

    @Throws(ReaderException::class)
    fun read(): T

    @Throws(ReaderException::class)
    fun read(count: Int): arrayOfT

    @Throws(ReaderException::class)
    fun skip(count: Int)
}