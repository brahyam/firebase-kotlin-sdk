version = "0.2.0"

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.quittle.android-emulator") version "0.2.0"
}

buildscript {
    repositories {
        jcenter()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
    }
}

android {
    compileSdkVersion(property("targetSdkVersion") as Int)
    defaultConfig {
        minSdkVersion(property("minSdkVersion") as Int)
        targetSdkVersion(property("targetSdkVersion") as Int)
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
    packagingOptions {
        pickFirst("META-INF/kotlinx-serialization-runtime.kotlin_module")
        pickFirst("META-INF/AL2.0")
        pickFirst("META-INF/LGPL2.1")
    }
}

// Optional configuration
androidEmulator {
    emulator {
        name("givlive_emulator")
        sdkVersion(28)
        abi("x86_64")
        includeGoogleApis(true) // Defaults to false
    }

    headless(true) // Defaults to false but should be set to true for most CI systems
}

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios")

    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
        )
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-functions:19.0.1")
            }
        }
        val iosMain by getting {}
        val jsMain by getting {}

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebasefunctions by cinterops.creating {
                    packageName("cocoapods.FirebaseFunctions")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseFunctions.def"))
                    compilerOpts("-F${rootProject.buildDir}/Firebase/FirebaseFunctions")
                }
            }
        }

        cocoapods {
            summary = ""
            homepage = ""
        }
    }
}

signing {
    sign(publishing.publications)
}
