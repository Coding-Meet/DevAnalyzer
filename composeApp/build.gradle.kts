import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}
apply(from = "../versioning.gradle.kts")

group = "com.meet"

val appVersionName: () -> String by extra
val appVersionCode: () -> Int by extra

java {
    toolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvm()
    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform - UI Framework
            implementation(libs.runtime)                    // Compose runtime for state management
            implementation(libs.foundation)                 // Foundation layouts and components
            implementation(libs.ui)                        // Core UI components
            implementation(libs.components.resources)       // Resource handling
            implementation(libs.ui.tooling.preview) // Preview support

            // Lifecycle - ViewModel and state management
            implementation(libs.androidx.lifecycle.viewmodel.compose)         // ViewModel for business logic
            implementation(libs.androidx.lifecycle.runtime.compose)   // Lifecycle-aware Compose

            // Material Design 3 - Enhanced design system
            implementation(libs.material3)                  // Material3 design system
            implementation(libs.material3.adaptive.navigation.suite) // Material3 adaptive design suite
            implementation(libs.material.icons.extended) // Extended Material icons
            implementation(libs.material3.adaptive)     // Material3 adaptive

            // Navigation - Type-safe screen navigation
            implementation(libs.androidx.navigation.compose)   // Compose navigation

            // Local Storage - DataStore for preferences
            implementation(libs.datastore.preferences)         // DataStore for user preferences
            implementation(libs.datastore.core)               // DataStore core functionality

            // Image Loading - Coil3 for loading images
            implementation(libs.coil.compose)                  // Coil3 Compose integration
            implementation(libs.coil.compose.core)            // Coil3 core functionality
            implementation(libs.coil.network.ktor3)           // Coil3 network loading with Ktor
            implementation(libs.coil.mp)                      // Coil3 multiplatform support

            // Dependency Injection - Koin for DI
            api(libs.koin.core)                     // Koin dependency injection core
            implementation(libs.koin.compose)                  // Koin integration with Compose
            implementation(libs.koin.composeVM)                // Koin ViewModel integration

            // Serialization - JSON parsing
            implementation(libs.kotlinx.serialization.json)    // JSON serialization

            // Logging - Kermit for multiplatform logging
            implementation(libs.kermit)                        // TouchLab Kermit logger

            // File Handling - File kit for file handling
            implementation(libs.filekit.dialogs)                // File kit dialogs for file handling
            implementation(libs.filekit.dialogs.compose)        // File kit dialogs for Compose

            // Serialization/Deserialization of toml format
            implementation(libs.akuleshov7.ktoml.core)

            // Semantic Versioning library
            implementation(libs.github.z4kn4fein.semver)

            // Theme Changer
            implementation(libs.jsystemthemedetector)

            // Crash Report
            api(libs.sentry)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            // Compose Desktop
            implementation(compose.desktop.currentOs)          // Platform-specific desktop support

            // Coroutines Swing
            implementation(libs.kotlinx.coroutines.swing)      // Swing dispatcher for desktop UI
        }
    }
}


compose {
    resources {
        packageOfResClass = "com.meet.dev.analyzer"
        generateResClass = always
    }
    desktop {
        application {
            mainClass = "com.meet.dev.analyzer.MainKt"
//            javaHome = System.getenv("JAVA_HOME")
//            javaHome = "/Users/meet/Library/Java/JavaVirtualMachines/ms-17.0.15/Contents/Home"

            nativeDistributions {
                targetFormats(
                    TargetFormat.Dmg, TargetFormat.Pkg, // macOS
                    TargetFormat.Msi, TargetFormat.Exe, // Windows
                    TargetFormat.Deb                    // Linux
                )
                packageName = "DevAnalyzer"
                packageVersion = appVersionName()
                includeAllModules = true
                description = "Deep insights into your development environment."
                vendor = "Meet"

                copyright = "© 2025 Meet. All rights reserved."
                licenseFile.set(project.file("../LICENSE"))


                buildTypes.release.proguard {
                    obfuscate = true
                    optimize = true
                    configurationFiles.from(project.file("compose-desktop.pro"))
                }
                val iconsRoot = project.file("src/jvmMain/resources/icons/")
                macOS {
                    iconFile.set(iconsRoot.resolve("app_logo.icns"))
                    minimumSystemVersion = "12.0"
                    bundleID = "com.meet.dev.analyzer"
                    appCategory = "Productivity"
                }
                windows {
                    iconFile.set(iconsRoot.resolve("app_logo.ico"))
                    perUserInstall = true
                    console = false
                    dirChooser = true
                    shortcut = true
                    menu = true
                    includeAllModules = true
                }
                linux {
                    iconFile.set(iconsRoot.resolve("app_logo.png"))
                    shortcut = true
                    appCategory = "Productivity"
                }

            }
        }
    }
}