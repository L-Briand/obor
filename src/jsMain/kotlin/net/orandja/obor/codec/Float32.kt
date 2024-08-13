package net.orandja.obor.codec

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get

actual fun float64toFloat32(value: Double): Float = Float32Array(1).apply { set(arrayOf(value.asDynamic()), 0) }[0]