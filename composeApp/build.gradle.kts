import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(compose.materialIconsExtended)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "com.revzion.siitglobe"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.revzion.siitglobe"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.0.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.1")
}

compose.desktop {
    application {
        mainClass = "com.revzion.siitglobe.MainKt"

        // -Xdock: flags are macOS-only — passing them on Windows crashes the JVM
        if (System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true) {
            jvmArgs += listOf(
                "-Xdock:name=SiiT",
                "-Xdock:icon=${project.file("src/jvmMain/resources/icon.png").absolutePath}",
            )
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SiiT"
            packageVersion = "1.0.3"
            description = "SiiT Student Management"
            copyright = "© 2024 Revzion"
            vendor = "Revzion"

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                bundleID = "com.revzion.siitglobe"
                appCategory = "public.app-category.education"
            }

            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                menuGroup = "SiiT"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                // Fixed UUID — Windows uses this to detect the existing installation.
                // When a user runs a newer MSI, Windows Installer will prompt to upgrade
                // (remove old version and install new) or abort. Same UUID across all versions.
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
            }

            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                packageName = "siit"
                debMaintainer = "support@revzion.com"
                menuGroup = "Education"
                appCategory = "Education"
            }
        }
    }
}
