rootProject.name = "SinkSabre"

include(":androidApp")
include(":desktopApp")
include(":shared")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.toastbits.dev/")
    }

    plugins {
        val kotlin_version: String = extra["kotlin.version"] as String
        val agp_version: String = extra["agp.version"] as String
        val compose_version: String = extra["compose.version"] as String

        kotlin("jvm").version(kotlin_version)
        kotlin("multiplatform").version(kotlin_version)
        kotlin("android").version(kotlin_version)

        id("com.android.application").version(agp_version)
        id("com.android.library").version(agp_version)

        id("org.jetbrains.compose").version(compose_version)

        id("io.github.gradle-nexus.publish-plugin").version("2.0.0")

        id("dev.toastbits.gradleremoterunner").version("0.0.5")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://jitpack.io")
    }
}
