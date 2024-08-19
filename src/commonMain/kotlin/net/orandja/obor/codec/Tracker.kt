@file:Suppress("NOTHING_TO_INLINE")

package net.orandja.obor.codec

inline fun Array<Long>.getFlag(index: Int, flag: Long) = get(index) and flag != 0L
inline fun Array<Long>.setFlag(index: Int, flag: Long, op: Boolean) {
    set(index, if (op) get(index) or flag else get(index) and flag.inv())
}

// region DECODER

internal inline fun newDecoderTracker() = arrayOf(0L, 0L, 0L, 0L, 0L, 0L)
internal inline fun newDecoderTracker(parent: Array<Long>) = arrayOf(0L, 0L, 0L, 0L, parent[0], parent[1])

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