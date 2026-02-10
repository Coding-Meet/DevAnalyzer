package com.meet.dev.analyzer.data.models.setting

enum class PathPickerType(
    val title: String,
    val description: String,
    val expectedStructure: String
) {

    /* ---------------- ANDROID SDK ---------------- */

    ANDROID_SDK(
        title = "Select Android SDK Path",
        description = "Root directory of the Android SDK installation used by Android Studio.",
        expectedStructure = "Must contain: platforms/ and build-tools/ directories"
    ),

    /* ---------------- GRADLE ---------------- */

    GRADLE_HOME(
        title = "Select Gradle User Home",
        description = "Gradle user home directory where caches, wrapper distributions, and daemons are stored.",
        expectedStructure = "Must contain: caches/ or wrapper/ directories"
    ),

    /* ---------------- AVD ---------------- */

    AVD_LOCATION(
        title = "Select AVD Location",
        description = "Directory where Android Virtual Device (AVD) configuration files are stored.",
        expectedStructure = "Must contain: *.ini files or *.avd directories"
    ),

    /* ---------------- .ANDROID ---------------- */

    ANDROID_FOLDER(
        title = "Select .android Folder",
        description = "Android configuration directory in the user home, usually ~/.android.",
        expectedStructure = "Must contain: avd/ directory"
    ),

    /* ---------------- KOTLIN / KONAN ---------------- */

    KONAN_FOLDER(
        title = "Select .konan Folder",
        description = "Kotlin/Native compiler cache and dependency directory, usually ~/.konan.",
        expectedStructure = "Must contain: dependencies/ or kotlin-native* directories"
    ),

    /* ---------------- JDK ---------------- */

    JDK_1(
        title = "Select JDK Path 1",
        description = "JDK installation directory or a root folder containing multiple JDK installations.",
        expectedStructure = "May contain: bin/, lib/, release, Contents/Home (macOS), or multiple JDK folders"
    ),

    JDK_2(
        title = "Select JDK Path 2",
        description = "Additional JDK installation directory or JDK root folder (optional).",
        expectedStructure = "May contain: bin/, lib/, release, Contents/Home (macOS), or multiple JDK folders"
    ),

    JDK_3(
        title = "Select JDK Path 3",
        description = "Additional JDK installation directory or JDK root folder (optional).",
        expectedStructure = "May contain: bin/, lib/, release, Contents/Home (macOS), or multiple JDK folders"
    ),

    /* ---------------- GOOGLE IDE ROOTS ---------------- */

    IDE_GOOGLE_1(
        title = "Select Google IDE Root (PROGRAM_FILES / CACHES)",
        description = "Root directory containing Google IDE installations such as Android Studio.",
        expectedStructure = "Should contain: Google/AndroidStudio<version> folders"
    ),

    IDE_GOOGLE_2(
        title = "Select Google IDE Root (LOCAL / LOGS)",
        description = "Secondary Google IDE directory used for logs or local application data.",
        expectedStructure = "Should contain: Google/AndroidStudio<version> folders"
    ),

    IDE_GOOGLE_3(
        title = "Select Google IDE Root (ROAMING / SUPPORT)",
        description = "Additional Google IDE directory used for configuration or support files.",
        expectedStructure = "Should contain: Google/AndroidStudio<version> folders"
    ),

    /* ---------------- JETBRAINS IDE ROOTS ---------------- */

    IDE_JETBRAINS_1(
        title = "Select JetBrains IDE Root (PROGRAM_FILES / CACHES)",
        description = "Root directory containing JetBrains IDE installations.",
        expectedStructure = "Should contain: JetBrains/Idea*, WebStorm*, PhpStorm*, Rider* folders"
    ),

    IDE_JETBRAINS_2(
        title = "Select JetBrains IDE Root (LOCAL / LOGS)",
        description = "Secondary JetBrains IDE directory used for logs or local application data.",
        expectedStructure = "Should contain: JetBrains/Idea*, WebStorm*, PhpStorm*, Rider* folders"
    ),

    IDE_JETBRAINS_3(
        title = "Select JetBrains IDE Root (ROAMING / SUPPORT)",
        description = "Additional JetBrains IDE directory used for configuration or support files.",
        expectedStructure = "Should contain: JetBrains/Idea*, WebStorm*, PhpStorm*, Rider* folders"
    );
}