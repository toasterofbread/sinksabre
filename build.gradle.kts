plugins {
    kotlin("multiplatform").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)
    id("org.jetbrains.compose").apply(false)
    id("io.github.gradle-nexus.publish-plugin")
}

allprojects {
    group = "dev.toastbits.sinksabre"
    version = "1.0.1"
}
