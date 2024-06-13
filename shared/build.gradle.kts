import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.0"
    id("com.android.library")
    id("org.jetbrains.compose")

    `maven-publish`
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm("desktop")

    sourceSets {
        all {
            languageSettings.optIn("io.ktor.util.InternalAPI")
        }

        val composekit_version: String = extra["composekit.version"] as String
        val ktor_version: String = extra["ktor.version"] as String

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-cio:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("media.kamel:kamel-image:0.9.4")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")

                implementation("dev.toastbits.composekit:library-android:$composekit_version")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)

                implementation("dev.toastbits.composekit:library-desktop:$composekit_version")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "dev.toastbits.sinksabre.shared"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

publishing {
    publications {
        afterEvaluate { afterEvaluate {
            withType<MavenPublication> {
                artifactId = artifactId.replace("shared", "sinksabre")

                artifact(tasks.register("${name}JavadocJar", Jar::class) {
                    archiveClassifier.set("javadoc")
                    archiveAppendix.set(this@withType.name)
                })

                pom {
                    name.set("sinksabre")
                    description.set("An alternative to the Beat Saber sync function provided by BMBF and BeatSaver for Oculus Quest")
                    url.set("https://github.com/toasterofbread/sinksabre")

                    licenses {
                        license {
                            name.set("GPL-3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                    developers {
                        developer {
                            id.set("toasterofbread")
                            name.set("Talo Halton")
                            email.set("talohalton@gmail.com")
                            url.set("https://github.com/toasterofbread")
                        }
                    }
                    scm {
                        connection.set("https://github.com/toasterofbread/sinksabre.git")
                        url.set("https://github.com/toasterofbread/sinksabre")
                    }
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/toasterofbread/sinksabre/issues")
                    }
                }
            }
        } }
    }
}
