package com.meet.dev.analyzer.data.models.setting

data class PathSettings(
    val sdkPath: String,
    val gradleUserHomePath: String,
    val avdLocationPath: String,
    val androidFolderPath: String,
    val konanFolderPath: String,

    val jdkPath1: String,
    val jdkPath2: String,
    val jdkPath3: String,

    val ideJetBrains1: String,
    val ideJetBrains2: String,
    val ideJetBrains3: String,

    val ideGoogle1: String,
    val ideGoogle2: String,
    val ideGoogle3: String
)