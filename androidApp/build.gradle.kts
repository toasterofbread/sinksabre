import java.util.Properties
import java.io.FileInputStream
import com.android.build.api.dsl.ApplicationVariantDimension

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))

                val composekit_version: String = extra["composekit.version"] as String
                implementation("dev.toastbits.composekit:library-android:$composekit_version")
            }
        }
    }
}

var keystore_props_file: File = rootProject.file("androidApp/keystore.properties")
if (!keystore_props_file.isFile) {
    keystore_props_file = rootProject.file("androidApp/keystore.properties.debug")
}

val keystore_props: Properties = Properties()
keystore_props.load(FileInputStream(keystore_props_file))

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "dev.toastbits.sinksabre"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    val version_name: String = findProperty("sinksabre.version.name") as String

    defaultConfig {
        applicationId = "dev.toastbits.sinksabre"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()

        versionCode = (findProperty("sinksabre.version.code") as String).toInt()
        versionName = version_name
    }

    signingConfigs {
        create("main") {
            storeFile = file(keystore_props["storeFile"] as String)
            storePassword = keystore_props["storePassword"] as String
            keyAlias = keystore_props["keyAlias"] as String
            keyPassword = keystore_props["keyPassword"] as String
        }
    }

    buildTypes {
        fun ApplicationVariantDimension.getApkName(): String =
            rootProject.name.lowercase() + "-" + version_name + applicationIdSuffix?.replace(".", "-").orEmpty() + ".apk"

        getByName("debug") {
            applicationIdSuffix = ".debug"
            setProperty("archivesBaseName", getApkName())

            signingConfig = signingConfigs.getByName("main")
        }
        getByName("release") {
            setProperty("archivesBaseName", getApkName())

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("main")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()

            isUniversalApk = true
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}
