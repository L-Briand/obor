import kotlinx.benchmark.gradle.benchmark
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.benchmark)
    id("maven-publish")
    id("signing")
    kotlin("plugin.allopen") version "1.9.20"
}

fun findProperty(name: String): String? = if (hasProperty(name)) property(name) as String else System.getenv(name)

group = findProperty("group")!!
version = findProperty("version")!!

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
        withSourcesJar(true)
        testRuns.named("test") {
            executionTask.configure { useJUnitPlatform() }
        }
    }

    // web

    js(IR) {
        browser {
            useCommonJs()
            useEsModules()
        }
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    linuxX64()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(project(":"))
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
        register("js")
        register("wasmJs")
        register("linuxX64")
    }

    configurations {
        named("main") {
            warmups = 20
            iterations = 10
            iterationTime = 3
            iterationTimeUnit = "s"
        }

        register("smoke") {
            warmups = 5
            iterations = 3
            iterationTime = 3000
            iterationTimeUnit = "ms"
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}