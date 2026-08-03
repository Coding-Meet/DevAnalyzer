<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="screenshot/dark_mode_logo_background_transparent.svg" />
    <img alt="DevAnalyzer" src="screenshot/light_mode_logo_background_transparent.svg" width="200" />
  </picture>
</p>

<h1 align="center">DevAnalyzer</h1>

<p align="center">
  <strong>Analyze, optimize, and clean your development environment.</strong>
</p>

<p align="center">
  <a href="https://coding-meet.github.io/DevAnalyzer/"><strong>Visit Website</strong></a>
</p>

DevAnalyzer is a cross-platform desktop application built with Compose Multiplatform and Kotlin Multiplatform (KMP) to help Android and Kotlin developers understand, optimize, and maintain their development environment.

---

## Overview

* **Project Analyzer**: Analyze modules, Gradle configurations, plugins, dependencies, and project metadata in a single view.
* **Clean Build**: Scan projects for build folders across all modules and safely delete them to reclaim space.
* **Workspace Analyzer**: Scan multiple workspaces to detect actively used Android SDKs, NDKs, CMake versions, and Gradle wrappers, highlighting what can be safely removed.
* **Storage Analyzer**: View storage consumed by Android SDKs, Gradle caches, IDE caches, Kotlin/Native, and AVDs.
* **Settings**: Configure custom toolchain paths and manage privacy options.

---

## Features

### Project Analyzer
* Analyze Gradle modules, plugins, and dependencies.
* List applied plugins and Version Catalog libraries.
* Browse Gradle build files and project configuration scripts directly inside the app.
* Inspect project metadata including:
  * Kotlin Version
  * Android Gradle Plugin (AGP)
  * Gradle Version
  * Compile, Target, and Min SDKs
  * NDK and CMake versions

### Clean Build
* Scan Android Studio projects for module build folders.
* Display expandable project and module layout.
* Compute folder sizes in real-time.
* Perform batch deletion of selected build folders with a safety confirmation dialog.
* View space recovery summaries after cleaning.

### Workspace Analyzer
* Analyze multiple workspace directories simultaneously.
* Cross-reference installed SDK tools, NDKs, CMake versions, and Gradle caches against all projects.
* Automatically protect active versions referenced by existing projects.
* Detect unused resources:
  * Android SDK Platforms
  * SDK Build Tools
  * SDK Sources
  * NDK & CMake Versions
  * Gradle Wrapper Caches
  * Kotlin/Native Prebuilts

### Storage Analyzer
* View storage allocations across:
  * Android SDK
  * Gradle Caches
  * Kotlin/Native
  * JDK
  * Android Virtual Devices (AVD)
  * IDE Caches and configuration folders
* Track real-time scan progress with elapsed timers.
* Support custom, non-standard JDK and IDE locations.

### Settings
* Custom paths configuration (SDK, Gradle Home, Kotlin Native, JDK, Android Studio, IntelliJ).
* Automatic path validation with quick-reset options.
* Toggle local crash logging, crash reporting, and usage analytics.

---

## Privacy & Analytics

Privacy is a core design principle of DevAnalyzer:
* **Local Processing**: All analysis runs locally. Your code, project names, and file paths are never uploaded.
* **Optional Analytics**: You can opt in or out of anonymous analytics at any time.
* **Zero PII**: No email addresses, personal details, or sensitive metadata are tracked.
* **SDK Configuration**: We use `PersonProfiles.IDENTIFIED_ONLY` internally in the PostHog SDK configuration. Since we never call `PostHog.identify`, all users remain completely anonymous.

---

## Feedback & Updates

* **In-App Feedback**: Rate the app, suggest features, and report issues using the built-in feedback dialog.
* **Update Verification**: Silently checks for new GitHub releases on startup and displays release notes inside the app.

---

## Tech Stack

| Category | Libraries & Tools |
|---|---|
| **Framework** | Compose Multiplatform |
| **Language** | Kotlin 2.x (Multiplatform) |
| **Design System** | Material 3 with Adaptive Navigation Suite |
| **Dependency Injection** | Koin |
| **Network** | Ktor |
| **Navigation** | Jetpack Navigation for Compose |
| **Image Loading** | Coil3 |
| **Local Storage** | DataStore (Core + Preferences) |
| **File Handling** | FileKit |
| **Semantic Versioning** | SemVer |
| **Analytics** | PostHog JVM (Anonymous Analytics) |
| **Crash Reporting** | Sentry JVM (Optional) |

---

## How to Run

### Run from Source
If you want to build and run the app from source:

**macOS / Linux**
```shell
./gradlew :composeApp:run
```

**Windows**
```shell
.\gradlew.bat :composeApp:run
```

### Installation Packages
Download the latest binaries from the [Releases](https://github.com/Coding-Meet/DevAnalyzer/releases) page.

* **Windows**: Download the `.msi` installer.
* **macOS**: Download the `.dmg` file. (If blocked by security settings, go to **System Settings → Privacy & Security → Allow Anyway** to run).
* **Linux**: Install the `.deb` package using:
  ```shell
  sudo dpkg -i devanalyzer_1.0.0-1_amd64.deb
  ```

---

## Support

If this project helped you, please give it a star! ⭐

<a href="https://www.buymeacoffee.com/codingmeet" target="_blank">
<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" width="160">
</a>

Your support is greatly appreciated.