plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
    signing
    `maven-publish`
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
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
        kotlinOptions.jvmTarget = "1.8"
    }

    test { useJUnitPlatform() }
}

java {
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("Obor") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("Obor")
                description.set("A CBOR serializer implemented with kotlinx-serialization")
                url.set("https://github.com/L-Briand/obor")
                licenses {
                    license {
                        name.set("BSD 3-Clause License")
                        url.set("https://opensource.org/licenses/BSD-3-Clause")
                    }
                }
                developers {
                    developer {
                        id.set("L-Briand")
                        name.set("Lionel Briand")
                        email.set("lionel.briand@orandja.fr")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/L-Briand/obor.git")
                    developerConnection.set("scm:git:ssh://github.com/L-Briand/obor.git")
                    url.set("https://github.com/L-Briand/obor")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OssSonatype"
            credentials {
                username = findProperty("ossrhUsername") as String
                password = findProperty("ossrhPassword") as String
            }
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    sign(publishing.publications["Obor"])
}