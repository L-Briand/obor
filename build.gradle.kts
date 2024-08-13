import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    id("maven-publish")
    id("signing")
}

fun findProperty(name: String): String? = if (hasProperty(name)) property(name) as String else System.getenv(name)
fun findFilledProperty(name: String): String? = findProperty(name)?.ifBlank { null }

group = findProperty("group")!!
version = findProperty("version")!!

val ossrhUsername = findFilledProperty("ossrh.username")
val ossrhPassword = findFilledProperty("ossrh.password")
val ossrhMavenEnabled = ossrhUsername != null && ossrhPassword != null
val isSigningEnabled = findFilledProperty("signing.keyId") != null &&
        findFilledProperty("signing.password") != null &&
        findFilledProperty("signing.secretKeyRingFile") != null

repositories {
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
    wasmJs { d8() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi { nodejs() }

    // https://kotlinlang.org/docs/native-target-support.html

    // Tier1

    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier2

    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        val publicationName = this@withType.name
        val javadocJar = tasks.register("${publicationName}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveBaseName.set("${archiveBaseName.get()}-${publicationName}")
        }
        artifact(javadocJar)
        pom {
            name.set(findProperty("POM_NAME")!!)
            description.set(findProperty("POM_DESCRIPTION")!!)
            url.set(findProperty("POM_URL")!!)
            licenses {
                license {
                    name.set(findProperty("POM_LICENSE_NAME")!!)
                    url.set(findProperty("POM_LICENSE_URL")!!)
                }
            }
            developers {
                developer {
                    id.set(findProperty("POM_DEVELOPER_LBRIAND_ID")!!)
                    name.set(findProperty("POM_DEVELOPER_LBRIAND_NAME")!!)
                    email.set(findProperty("POM_DEVELOPER_LBRIAND_EMAIL")!!)
                }
            }
            scm {
                connection.set(findProperty("POM_SCM_URL")!!)
                developerConnection.set(findProperty("POM_SCM_CONNECTION")!!)
                url.set(findProperty("POM_SCM_DEV_CONNECTION")!!)
            }
        }
    }

    repositories {
        mavenLocal()
        if (ossrhMavenEnabled) {
            maven {
                name = "sonatype"
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
}

if (isSigningEnabled) {
    signing {
        sign(publishing.publications)
    }
}

tasks.create<Delete>("cleanupGithubDocumentation") {
    delete(file("docs"))
}
tasks.create<Copy>("generateGithubDocumentation") {
    dependsOn("cleanupGithubDocumentation")
    dependsOn("dokkaHtml")
    val buildDir = layout.buildDirectory
    from(buildDir.dir("dokka/html")).into("docs")
}