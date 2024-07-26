package net.orandja.obor

@OptIn(ExperimentalStdlibApi::class)
object Resource {

    private fun String.hex() = this.hexToByteArray(HexFormat.UpperCase)

    val FALSE = "F4".hex()
    val TRUE = "F5".hex()

    object MajorPositive {
        val EMPTY = "00".hex()
        val SIZE_0 = "17".hex()
        val SIZE_8 = "1880".hex()
        val SIZE_16 = "198000".hex()
        val SIZE_32 = "1A80000000".hex()
        val SIZE_64 = "1B1000000000000000".hex()
        val LIMIT = "1B8000000000000000".hex()
    }

    object MajorNegative {
        val EMPTY = "20".hex()
        val SIZE_0 = "37".hex()
        val SIZE_8 = "3880".hex()
        val SIZE_16 = "398000".hex()
        val SIZE_32 = "3A80000000".hex()
        val SIZE_64 = "3B1000000000000000".hex()
        val LIMIT = "3B8000000000000000".hex()
    }
}