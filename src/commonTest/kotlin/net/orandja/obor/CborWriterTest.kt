package net.orandja.obor

import kotlin.test.Test
import kotlin.test.assertContentEquals

class CborWriterTest {
    @Test
    fun testWriter() {
        // Write raw bytes
        assertContentEquals("61".hex(), buildCbor { write(0x61) })
        assertContentEquals("FF".hex(), buildCbor { write(0xFFu) })
        assertContentEquals("616263".hex(), buildCbor { write(byteArrayOf(0x61, 0x62, 0x63), 0, 3) })
        assertContentEquals("62".hex(), buildCbor { write(byteArrayOf(0x61, 0x62, 0x63), 1, 1) })

        // @formatter:off

        // Write major
        assertContentEquals("26".hex(), buildCbor { writeMajor8 (0x20u, 0x06u) })
        assertContentEquals("26".hex(), buildCbor { writeMajor16(0x20u, 0x06u) })
        assertContentEquals("26".hex(), buildCbor { writeMajor32(0x20u, 0x06u) })
        assertContentEquals("26".hex(), buildCbor { writeMajor64(0x20u, 0x06u) })

        assertContentEquals("3861".hex(), buildCbor { writeMajor8 (0x20u, 0x61u) })
        assertContentEquals("3861".hex(), buildCbor { writeMajor16(0x20u, 0x61u) })
        assertContentEquals("3861".hex(), buildCbor { writeMajor32(0x20u, 0x61u) })
        assertContentEquals("3861".hex(), buildCbor { writeMajor64(0x20u, 0x61u) })

        assertContentEquals("396162".hex(), buildCbor { writeMajor16(0x20u, 0x6162u) })
        assertContentEquals("396162".hex(), buildCbor { writeMajor32(0x20u, 0x6162u) })
        assertContentEquals("396162".hex(), buildCbor { writeMajor64(0x20u, 0x6162u) })

        assertContentEquals("3A61626364".hex(), buildCbor { writeMajor32(0x20u, 0x61626364u) })
        assertContentEquals("3A61626364".hex(), buildCbor { writeMajor64(0x20u, 0x61626364u) })

        assertContentEquals("3B6162636465666768".hex(), buildCbor { writeMajor64(0x20u, 0x6162636465666768u) })

        // Write headers
        assertContentEquals("6106".hex(),               buildCbor { writeHeader8 (0x61u, 0x06u) })
        assertContentEquals("610006".hex(),             buildCbor { writeHeader16(0x61u, 0x06u) })
        assertContentEquals("6100000006".hex(),         buildCbor { writeHeader32(0x61u, 0x06u) })
        assertContentEquals("610000000000000006".hex(), buildCbor { writeHeader64(0x61u, 0x06u) })

        assertContentEquals("6161".hex(),               buildCbor { writeHeader8 (0x61u, 0x61u) })
        assertContentEquals("610061".hex(),             buildCbor { writeHeader16(0x61u, 0x61u) })
        assertContentEquals("6100000061".hex(),         buildCbor { writeHeader32(0x61u, 0x61u) })
        assertContentEquals("610000000000000061".hex(), buildCbor { writeHeader64(0x61u, 0x61u) })

        assertContentEquals("616162".hex(),             buildCbor { writeHeader16(0x61u, 0x6162u) })
        assertContentEquals("6100006162".hex(),         buildCbor { writeHeader32(0x61u, 0x6162u) })
        assertContentEquals("610000000000006162".hex(), buildCbor { writeHeader64(0x61u, 0x6162u) })

        assertContentEquals("6161626364".hex(),         buildCbor { writeHeader32(0x61u, 0x61626364u) })
        assertContentEquals("610000000061626364".hex(), buildCbor { writeHeader64(0x61u, 0x61626364u) })

        assertContentEquals("616162636465666768".hex(), buildCbor { writeHeader64(0x61u, 0x6162636465666768u) })

    }
}