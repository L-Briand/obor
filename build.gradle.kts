import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
}

group = "net.orandja.obor"
version = "0.1.0"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

tasks {
    val compile: KotlinCompile.() -> Unit = {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
        kotlinOptions.jvmTarget = "11"
    }
    compileKotlin(compile)
    compileTestKotlin(compile)

    test { useJUnitPlatform() }
}