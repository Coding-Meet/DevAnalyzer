import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            // 🎨 Compose Multiplatform - UI Framework (Default libraries)
            implementation(compose.runtime)                    // Compose runtime for state management
            implementation(compose.foundation)                 // Foundation layouts and components
            implementation(compose.ui)                        // Core UI components
            implementation(compose.components.resources)       // Resource handling
            implementation(compose.components.uiToolingPreview) // Preview support

            // 🔄 Lifecycle - ViewModel and state management (Default + Enhanced)
            implementation(libs.androidx.lifecycle.viewmodel.compose)         // ViewModel for business logic
            implementation(libs.androidx.lifecycle.runtime.compose)   // Lifecycle-aware Compose

            // 🎨 Additional Material Design 3 - Enhanced design system
            implementation(compose.material3)                  // Material3 design system
            implementation(compose.material3AdaptiveNavigationSuite) // adaptive design
            implementation(compose.materialIconsExtended) // Extended Material icons
            implementation(libs.material3.adaptive)     // adaptive design
            implementation(libs.material3.adaptive.layout) // adaptive layout
            implementation(libs.material3.adaptive.navigation) // adaptive navigation

            // 🧭 Navigation - Type-safe screen navigation
            implementation(libs.androidx.navigation.compose)   // Compose navigation

            // 💾 Local Storage - DataStore for preferences
            implementation(libs.datastore.preferences)         // DataStore for user preferences
            implementation(libs.datastore.core)               // DataStore core functionality


            // 💉 Dependency Injection - Koin for DI
            api(libs.koin.core)                     // Koin dependency injection core
            implementation(libs.koin.compose)                  // Koin integration with Compose
            implementation(libs.koin.composeVM)                // Koin ViewModel integration

            // 📦 Serialization - JSON parsing
            implementation(libs.kotlinx.serialization.json)    // JSON serialization

            // 📝 Logging - Kermit for multiplatform logging
            implementation(libs.kermit)                        // TouchLab Kermit logger

            // 📁 File Handling - Filekit for file handling
            implementation(libs.filekit.dialogs)                // Filekit dialogs for file handling
            implementation(libs.filekit.dialogs.compose)        // Filekit dialogs for Compose
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            // 🖥️ Compose Desktop (Default library)
            implementation(compose.desktop.currentOs)          // Platform-specific desktop support

            // ⚡ Coroutines Swing (Default library)
            implementation(libs.kotlinx.coroutines.swing)      // Swing dispatcher for desktop UI
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.meet.project.analyzer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.meet.project.analyzer"
            packageVersion = "1.0.0"
        }
    }
}
