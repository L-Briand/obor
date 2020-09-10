package net.orandja.obor

object ResourceLoader {
    private val loader = this.javaClass.classLoader
    private const val HEX_CHARS = "0123456789ABCDEF"

    // Found on https://gist.github.com/fabiomsr/845664a9c7e92bafb6fb0ca70d4e44fd
    private fun hexStringToByteArray(string: String): ByteArray {
        val len = string.length
        val result = ByteArray(len / 2)
        (0 until len step 2).forEach { i ->
            result[i.shr(1)] = HEX_CHARS.indexOf(string[i]).shl(4).or(HEX_CHARS.indexOf(string[i + 1])).toByte()
        }
        return result
    }

    fun loadCborHex(resource: String): Map<String, ByteArray> {
        val bytes = loader.getResource("$resource.cborhex")?.readBytes() ?: throw IllegalArgumentException("$resource not found")
        return String(bytes, Charsets.UTF_8).lines().asSequence()
            .map { it.split("=") }
            .filter { it.size == 2 }
            .map { it[0].trim() to it[1].trim() }
            .filter { it.second.isNotBlank() && it.second.all { it in HEX_CHARS } }
            .map { it.first to hexStringToByteArray(it.second) }
            .toMap()
    }
}