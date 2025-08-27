plugins {
    // 🚫 Apply plugins without configuring them (they will be configured in subprojects)

    // 🔧 Kotlin plugins - For multiplatform and serialization
    alias(libs.plugins.kotlinMultiplatform) apply false  // Kotlin Multiplatform plugin
    alias(libs.plugins.kotlinSerialization) apply false  // Kotlinx Serialization plugin

    // 🎨 Compose plugins - For UI framework
    alias(libs.plugins.composeMultiplatform) apply false // Compose Multiplatform plugin
    alias(libs.plugins.composeCompiler) apply false      // Compose Compiler plugin
    alias(libs.plugins.composeHotReload) apply false     // Hot reload for development
}