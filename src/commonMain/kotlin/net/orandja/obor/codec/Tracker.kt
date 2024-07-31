@file:Suppress("NOTHING_TO_INLINE")

package net.orandja.obor.codec

inline fun Array<Long>.getFlag(index: Int, flag: Long) = get(index) and flag != 0L
inline fun Array<Long>.setFlag(index: Int, flag: Long, op: Boolean) {
    set(index, if (op) get(index) or flag else get(index) and flag.inv())
}

// region DECODER

internal inline fun newDecoderTracker() = arrayOf(0L, 0L, 0L, 0L)
internal inline fun newDecoderTracker(parent: Array<Long>) = arrayOf(0L, 0L, parent[0], parent[1])

// @formatter:off
internal const val DEC_TAG =         0b01L
internal const val DEC_TAG_REQUIRE = 0b10L
// @formatter:on

internal inline var Array<Long>.decClassHasTag: Boolean
    get() = getFlag(0, DEC_TAG)
    set(value) = setFlag(0, DEC_TAG, value)

internal inline var Array<Long>.decClassRequireTag: Boolean
    get() = getFlag(0, DEC_TAG_REQUIRE)
    set(value) = setFlag(0, DEC_TAG_REQUIRE, value)

internal inline var Array<Long>.decClassTag: Long
    get() = get(1)
    set(value) = set(1, value)

internal inline var Array<Long>.decParentHasTag: Boolean
    get() = getFlag(2, DEC_TAG)
    set(value) = setFlag(2, DEC_TAG, value)

internal inline var Array<Long>.decParentRequireTag: Boolean
    get() = getFlag(2, DEC_TAG_REQUIRE)
    set(value) = setFlag(2, DEC_TAG_REQUIRE, value)

internal inline var Array<Long>.decParentTag: Long
    get() = get(3)
    set(value) = set(3, value)

// endregion

// region ENCODER

internal inline fun newEncoderTracker() = arrayOf(0L, 0L, 0L, 0L, 0L, 0L)
internal inline fun newEncoderTracker(parent: Array<Long>) = arrayOf(0L, 0L, 0L, 0L, parent[2], parent[3])

// @formatter:off
internal const val ENC_INFINITE  = 0b001L
internal const val ENC_RAW_BYTES = 0b010L
internal const val ENC_TAG       = 0b100L
// @formatter:on

internal inline var Array<Long>.encClassIsInfinite: Boolean
    get() = getFlag(0, ENC_INFINITE)
    set(value) = setFlag(0, ENC_INFINITE, value)

internal inline var Array<Long>.encClassIsRawBytes: Boolean
    get() = getFlag(0, ENC_RAW_BYTES)
    set(value) = setFlag(0, ENC_RAW_BYTES, value)

internal inline var Array<Long>.encClassHasTag: Boolean
    get() = getFlag(0, ENC_TAG)
    set(value) = setFlag(0, ENC_TAG, value)

internal inline var Array<Long>.encClassTag: Long
    get() = get(1)
    set(value) = set(1, value)

internal inline var Array<Long>.encFieldIsInfinite: Boolean
    get() = getFlag(2, ENC_INFINITE)
    set(value) = setFlag(2, ENC_INFINITE, value)

internal inline var Array<Long>.encFieldIsRawBytes: Boolean
    get() = getFlag(2, ENC_RAW_BYTES)
    set(value) = setFlag(2, ENC_RAW_BYTES, value)

internal inline var Array<Long>.encFieldHasTag: Boolean
    get() = getFlag(2, ENC_TAG)
    set(value) = setFlag(2, ENC_TAG, value)

internal inline var Array<Long>.encFieldTag: Long
    get() = get(3)
    set(value) = set(3, value)

internal inline var Array<Long>.encParentIsInfinite: Boolean
    get() = getFlag(4, ENC_INFINITE)
    set(value) = setFlag(4, ENC_INFINITE, value)

internal inline var Array<Long>.encParentIsRawBytes: Boolean
    get() = getFlag(4, ENC_RAW_BYTES)
    set(value) = setFlag(4, ENC_RAW_BYTES, value)

internal inline var Array<Long>.encParentHasTag: Boolean
    get() = getFlag(4, ENC_TAG)
    set(value) = setFlag(4, ENC_TAG, value)

internal inline var Array<Long>.encParentTag: Long
    get() = get(5)
    set(value) = set(5, value)

// endregion
