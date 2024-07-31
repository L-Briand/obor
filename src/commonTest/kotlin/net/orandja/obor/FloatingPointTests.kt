package net.orandja.obor

import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertContentEquals

class FloatingPointTests {
    companion object {
        val CBOR_FLOAT_0 = "F90000".hex()
        val CBOR_FLOAT_MINUS_0 = "F98000".hex()
        val CBOR_FLOAT_INFINITY = "F97C00".hex()
        val CBOR_FLOAT_MINUS_INFINITY = "F9FC00".hex()
        val CBOR_FLOAT_NAN = "F97E00".hex()

        val FLOAT_32_0 = 0.0F
        val FLOAT_32_MINUS_0 = -0.0F
        val FLOAT_32_INFINITY = Float.POSITIVE_INFINITY
        val FLOAT_32_MINUS_INFINITY = Float.NEGATIVE_INFINITY
        val FLOAT_32_NAN = Float.NaN

        val FLOAT_64_0 = 0.0
        val FLOAT_64_MINUS_0 = -0.0
        val FLOAT_64_INFINITY = Double.POSITIVE_INFINITY
        val FLOAT_64_MINUS_INFINITY = Double.NEGATIVE_INFINITY
        val FLOAT_64_NAN = Double.NaN
    }

    @Test
    fun floatConstants() {
        assertTransformation(CBOR_FLOAT_0, FLOAT_32_0)
        assertTransformation(CBOR_FLOAT_MINUS_0, FLOAT_32_MINUS_0)
        assertTransformation(CBOR_FLOAT_INFINITY, FLOAT_32_INFINITY)
        assertTransformation(CBOR_FLOAT_MINUS_INFINITY, FLOAT_32_MINUS_INFINITY)
        assertTransformation(CBOR_FLOAT_NAN, FLOAT_32_NAN)

        assertTransformation(CBOR_FLOAT_0, FLOAT_64_0)
        assertTransformation(CBOR_FLOAT_MINUS_0, FLOAT_64_MINUS_0)
        assertTransformation(CBOR_FLOAT_INFINITY, FLOAT_64_INFINITY)
        assertTransformation(CBOR_FLOAT_MINUS_INFINITY, FLOAT_64_MINUS_INFINITY)
        assertTransformation(CBOR_FLOAT_NAN, FLOAT_64_NAN)
    }

    @Test
    fun ordinaryFloatingPoints() {
        assertContentEquals("FBBE6FFFFFFFFFFFFF".hex(), -5.960464477539062e-8 encodeCbor serializer())
//        assertEquals(-5.960464477539062e-8, "FBBE6FFFFFFFFFFFFF".hex() decodeCbor  serializer<Double>())

//        assertContentEquals("F98001".hex(), -5.9604644775390625e-8 encodeCbor serializer())
//        assertEquals(-5.9604644775390625e-8, "F98001".hex() decodeCbor  serializer<Double>())

//        assertContentEquals("FBBE70000000000001".hex(), -5.960464477539064e-8 encodeCbor serializer())
//        assertEquals(-5.960464477539064e-8, "FBBE70000000000001".hex() decodeCbor  serializer<Double>())
        // draft-rundgren-deterministic-cbor-17 -> Section 2.3.3. "Ordinary" Floating Point Numbers
        assertTransformation("F98001".hex(), -5.9604644775390625e-8)
        assertTransformation("FBBE70000000000001".hex(), -5.960464477539064e-8)
        assertTransformation("FAB3800001".hex(), -5.960465188081798e-8)
        assertTransformation("F903FF".hex(), 0.00006097555160522461)
        assertTransformation("F97BFF".hex(), 65504.0)
        assertTransformation("FA477FE001".hex(), 65504.00390625)
        assertTransformation("FA47800000".hex(), 65536.0)
        assertTransformation("FA4128F5C1".hex(), 10.559998512268066)
        assertTransformation("FB40251EB820000001".hex(), 10.559998512268068)
        assertTransformation("FA7F7FFFFF".hex(), 3.4028234663852886e+38)
        assertTransformation("FB47EFFFFFE0000001".hex(), 3.402823466385289e+38)
        assertTransformation("FA00000001".hex(), 1.401298464324817e-45)
        assertTransformation("FA007FFFFF".hex(), 1.1754942106924411e-38)
        assertTransformation("FB0000000000000001".hex(), 5.0e-324)
        assertTransformation("FBFFEFFFFFFFFFFFFF".hex(), -1.7976931348623157e+308)
    }
}