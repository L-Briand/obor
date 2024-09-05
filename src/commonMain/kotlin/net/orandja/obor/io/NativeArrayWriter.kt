package net.orandja.obor.io

/**
 * Something that can write into a native array.
 */
interface NativeArrayWriter<T, in arrayOfT> {
    fun totalWrite(): Long

    @Throws(WriterException::class)
    fun write(value: T)

    @Throws(WriterException::class)
    fun write(array: arrayOfT, offset: Int, count: Int)
}