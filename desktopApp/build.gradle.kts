import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.toastbits.gradleremoterunner")
}

val version_name: String = extra["sinksabre.version.name"] as String

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared"))

                val composekit_version: String = extra["composekit.version"] as String
                implementation("dev.toastbits.composekit:library-desktop:$composekit_version")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = version_name
        }
    }
}
