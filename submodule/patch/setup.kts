import java.io.File

val root = File("${System.getProperty("user.dir")}/src")
println("${root.absolutePath}")
for (parent in root.listFiles()) {
    if (parent.isDirectory && parent.name != "commonMain") {
        val file = File(parent, "kotlin/net/orandja/obor/codec/Float32.kt")
        println("Patch ${file.absolutePath}")
        file.parentFile.mkdirs()
        file.writer(Charsets.UTF_8).use {
            it.write(
                """
                package net.orandja.obor.codec
                
                actual fun float64toFloat32(value: Double): Float = value.toFloat()
            """.trimIndent()
            )
        }
    }
}

println("Patching jsMain")
val jsMain = File(root, "jsMain/kotlin/net/orandja/obor/codec/Float32.kt")
jsMain.parentFile.mkdirs()
jsMain.writer(Charsets.UTF_8).use {
    it.write(
        """
            package net.orandja.obor.codec
            
            import org.khronos.webgl.Float32Array
            import org.khronos.webgl.get
            
            actual fun float64toFloat32(value: Double): Float = Float32Array(1).apply { set(arrayOf(value.asDynamic()), 0) }[0]            
        """.trimIndent()
    )
}