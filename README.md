<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/screenshot/dark_mode_logo_background_transparent.svg" />
    <img alt="DevAnalyzer" src="docs/screenshot/light_mode_logo_background_transparent.svg" width="200" />
  </picture>
</p>

<h1 align="center">DevAnalyzer</h1>

<p align="center">
  <strong>Analyze, optimize, and clean your Android & Multiplatform development environment.</strong>
</p>

<p align="center">
  <a href="https://coding-meet.github.io/DevAnalyzer/"><strong>Visit Website</strong></a>
</p>

DevAnalyzer is a cross-platform desktop application built with Compose Multiplatform and Kotlin Multiplatform (KMP) to help Android and Kotlin Multiplatform developers understand, optimize, and maintain their development environment.

---

## Overview

- **Project Analyzer**: Analyze modules, Gradle configurations, and dependencies across Android, iOS, JVM, JS, Wasm, and Server targets.
- **Clean Build**: Scan projects for build folders across all modules and safely batch-delete them to reclaim space.
- **Workspace Analyzer**: Cross-reference installed Android SDKs, NDKs, CMake, Kotlin/Native toolchains, and Gradle resources with your projects to identify active and unused resources.
- **Storage Analyzer**: View storage consumed by Android SDKs, Gradle caches, Kotlin/Native toolchains (LLVM/LLDB), JDK, AVDs, and IDE data.
- **Settings**: Configure custom toolchain paths with smart discovery and manage privacy options.

---

## Features

### Project Analyzer

- Analyze Gradle modules, plugins, and dependencies for **Android, iOS, JVM, JS, Wasm, and Server** targets.
- List applied plugins and Version Catalog (`libs.versions.toml`) libraries.
- Browse source files (Kotlin, Java, XML) and project configuration scripts directly inside the app.
- Inspect project metadata including:
  - Kotlin & AGP Versions
  - Gradle & Gradle Wrapper versions
  - Compile, Target, and Min SDKs
  - NDK and CMake versions

### Clean Build

- Scan Gradle projects for module build folders.
- Display expandable project and module layout.
- Compute folder sizes in real-time.
- Perform batch deletion of selected build folders with a safety confirmation dialog.
- View space recovery summaries after cleaning.

### Workspace Analyzer

- Analyze multiple workspace directories simultaneously.
- Cross-reference installed Android SDKs, NDKs, CMake versions, Kotlin/Native toolchains, and Gradle resources across all projects.
- Automatically identify resources referenced by active projects.
- Detect unused resources that can be reviewed for cleanup:
  - Android SDK Platforms
  - SDK Build Tools
  - SDK Sources
  - NDK & CMake Versions
  - Gradle Wrapper Caches
  - Gradle Dependency Cache
  - Kotlin/Native Prebuilts
  - Gradle Toolchain JDKs
  - Android System Images
  - Android Virtual Devices

### Storage Analyzer

- View storage allocations across:
  - Android SDK (Platforms, Build Tools, Sources, System Images)
  - Gradle Caches (Wrappers, Global Dependency Cache `modules-2`)
  - Kotlin/Native Toolchains (Konan prebuilts, LLVM, LLDB)
  - Installed JDKs
  - Android Virtual Devices (AVD)
  - IDE Data (Android Studio and IntelliJ IDEA configuration/cache)
- Track real-time scan progress with elapsed timers.
- Support custom, non-standard JDK and IDE locations.

### Settings & Discovery

- **Smart Path Discovery**: Automatically discover JDK, Android SDK, and IDE installations, including custom locations.
- **Custom Path Configuration**: Manage paths for SDK, Gradle, Kotlin Native, JDK, Android Studio, and IntelliJ.
- **Path Validation**: Automatic validation with one-click reset options.
- **Privacy Controls**: Toggle local crash logging, anonymous crash reporting, and usage analytics.

---

## Privacy & Analytics

Privacy is a core design principle of DevAnalyzer:

- **Local Processing**: All analysis runs locally. Your code, project names, and file paths are never uploaded.
- **Optional Analytics**: You can opt in or out of anonymous analytics at any time.
- **Zero PII**: No email addresses, personal details, or sensitive metadata are tracked.
- **Anonymous by Design**: Analytics are configured to remain completely anonymous. No user identification or personal information is collected.

---

## Feedback & Updates

- **In-App Feedback**: Rate the app, suggest features, and report issues using the built-in feedback dialog.
- **Update Verification**: Silently checks for new GitHub releases on startup and displays release notes inside the app.

---

## Tech Stack

| Category                 | Libraries & Tools                         |
| ------------------------ |-------------------------------------------|
| **Framework**            | Compose Multiplatform                     |
| **Language**             | Kotlin 2.x (Multiplatform)                |
| **Design System**        | Material 3 with Adaptive Navigation Suite |
| **Dependency Injection** | Koin                                      |
| **Network**              | Ktor                                      |
| **Navigation**           | Jetpack Navigation Compose                |
| **Image Loading**        | Coil3                                     |
| **Local Storage**        | DataStore (Core + Preferences)            |
| **File Handling**        | FileKit                                   |
| **Semantic Versioning**  | SemVer                                    |
| **Analytics**            | PostHog JVM (Anonymous Analytics)         |
| **Crash Reporting**      | Sentry JVM (Optional)                     |

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

Download the latest release from the [Releases](https://github.com/Coding-Meet/DevAnalyzer/releases) page.

- **Windows**: Download the `.msi` installer.
- **macOS**: Download the `.dmg` file. (If blocked by security settings, go to **System Settings → Privacy & Security → Allow Anyway** to run).
- **Linux**: Install the `.deb` package using:
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
