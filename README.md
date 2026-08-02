# 🧠 DevAnalyzer

[![DevAnalyzer](screenshot/dev_analyzer.png)](https://youtu.be/c3t1SIKSBBk?si=o77BGpvB2zwoFZPs)

**🌐 [Visit Website](https://coding-meet.github.io/DevAnalyzer/)**

---

## 🪄 Overview

**DevAnalyzer** is a cross-platform desktop application built with **Compose Multiplatform** and **Kotlin Multiplatform (KMP)**.

- 🧩 **Project Analyzer** — Examines project modules, Gradle configurations, applied plugins, dependencies, and related build files.
- 🧹 **Clean Build** — Scans Android Studio projects for build folders across all modules and enables selective deletion to reclaim disk space.
- 🏢 **Workspace Analyzer** — Combined scan of multiple project folders to cross-reference referenced Gradle/Kotlin/SDK/NDK/CMake versions against system assets and safely clean unused development resources.
- 💾 **Storage Analyzer** — Scans SDKs, IDE data, Gradle caches, and library directories to visualize overall storage usage.
- ⚙️ **Settings** — Configure custom paths for Android SDK, Gradle User Home, Kotlin Native, and IDE locations.

Designed for modern developers, **DevAnalyzer** helps you **understand**, **analyze**, and **optimize** your entire development ecosystem — all from a single, unified interface.  
Built as a **desktop-first tool**, it runs seamlessly across platforms and can be easily extended to **Android**, **KMP**, or **backend Kotlin** environments.

---

## 🚀 Features

### 🧩 Project Analyzer

- 🔍 Analyze **Gradle modules**, **plugins**, and **dependencies**.
- 📦 List all applied plugins and version catalogs.
- 📄 View **build files** and configuration scripts in an organized manner.
- 🧱 Inspect project metadata such as Gradle Kotlin, AGP, Min SDK, Compile SDK, Target SDK, NDK,
  CMake and Multi-Module.
- 🧾 Preview all **project and Gradle files** directly in the app.

### 🧹 Clean Build

- 🗂️ **Scan Android Studio projects** for build folders across all modules.
- 📊 **Visual project grouping** with expandable/collapsible module lists.
- 📏 **Real-time size calculation** for each module and total space usage.
- ✅ **Selective deletion** with tri-state checkboxes (project-level and module-level).
- 🎯 **Batch operations** - Select/Deselect all projects or individual modules.
- ⚠️ **Confirmation dialog** with 2-column grid layout showing all selected items.
- 🎨 **Floating action button** with smooth animations for quick delete access.
- 💾 **Space recovery tracking** - See exactly how much space you'll free up.

### 🏢 Workspace Analyzer

- 📂 **Multi-Workspace Path Persistence** — Add and persist multiple workspace root paths (e.g. `AndroidStudioProjects`, `IdeaProjects`) in a single run.
- 🔄 **Cross-Reference Dependency Auditing** — Traverses all projects under workspace directories to extract referenced compile/target SDK versions, Gradle wrappers, and Kotlin compilers.
- 🛡️ **Protected Active Resources** — Automatically locks and hides active system resources referenced by any of your workspace projects, showing a lock icon and project references.
- 🧹 **Unused System Resources Cleanup** — Highlights installed Android SDK Platforms, SDK Build Tools, SDK Sources, old Gradle wrapper versions, Kotlin Native compiler prebuilts, NDKs, CMake versions, and library cache distributions that are no longer in use.
- 📦 **Accordion Grouping Panels** — Groups scan results inside bordered nested category containers with dynamic rounded corners based on expanded state.
- ⚠️ **Context-Aware Safety Banners** — Prompts alert caution strips and warning sheets dynamically based on active search queries or tab filters.
- 💾 **State Persistence** — Saves the list of currently selected workspace folders in DataStore for seamless return.

### 💾 Storage Analyzer

- 💡 Get total storage summaries by component (SDK, IDE, Gradle, Library, etc.).
- 📊 Scan **SDK**, **NDK**, **CMake**, **Kotlin/Native**, **JDK** and **Extras** directories.
- 📄 Analyze **IDE data** (Android Studio, IntelliJ) including logs, caches, and support files.
- 🧠 Inspect **Gradle Daemons**, **Wrappers**, and **Cached Libraries**.
- ⚡ Real-time progress tracking with elapsed time display.
- 📁 Support for multiple JDK paths and IDE locations.

### ⚙️ Settings

- 🛠️ Configure custom paths for:
  - Android SDK location
  - Gradle User Home directory
  - Kotlin Native cache
  - Android Studio/IDE locations
- ✅ Path validation and existence status indicators
- 🔄 Reset to default paths option
- 🪵 Crash logging
- 📊 **Anonymous Analytics** — Optional, privacy-first PostHog integration to monitor scan speeds and usage frequency (completely anonymous; never collects file paths, projects, code, or PII). Can be disabled at any time.


### 💬 Feedback & Review

- ⭐ **Interactive Rating System** — Rate your experience with a touch-friendly star selector.
- 📋 **Detailed Suggestions & Checklists** — Share suggestions, select most-used features, and request future tools.
- 🔐 **Local Version Locking** — Prevents duplicate reviews for the same version using DataStore, unlocking automatically when you update.
- 🛡️ **Privacy & Anonymity** — Clear trust indicators ensuring reviews are anonymous unless you choose to share your email.

### 🔄 Silent Update Checks

- 🚀 **Silent Background Verification** — Checks for new releases on startup using Ktor without interrupting your work.
- 📋 **Release Notes View** — Displays a premium scrollable "What's New" release details card if an update is found.
- 🌐 **One-Click GitHub Redirection** — Prompts a direct link to open the latest release download page instantly.

---

## 🧰 Tech Stack

| Category                 | Libraries & Tools                               |
| ------------------------ | ----------------------------------------------- |
| **Framework**            | Compose Multiplatform                           |
| **Language**             | Kotlin 2.x (Multiplatform)                      |
| **Architecture**         | MVVM                                            |
| **Design System**        | Material 3 with Adaptive Navigation Suite       |
| **Dependency Injection** | Koin                                            |
| **Navigation**           | Jetpack Navigation for Compose                  |
| **Image Loading**        | Coil3 (Compose + Ktor + Multiplatform)          |
| **Local Storage**        | DataStore (Core + Preferences)                  |
| **Serialization**        | kotlinx.serialization                           |
| **Logging**              | Kermit (TouchLab Multiplatform Logger)          |
| **File Handling**        | FileKit (Dialogs + Compose)                     |
| **Theme Detection**      | JSystemThemeDetector                            |
| **Semantic Versioning**  | SemVer (z4kn4fein/semver)                       |
| **Coroutines**           | kotlinx.coroutines + Swing Dispatcher (Desktop) |
| **Sentry**               | Sentry (Crash Reporting)                        |
| **Analytics**            | PostHog (Anonymous Usage Analytics)              |
| **buildConfig**          | Buildkonfig                                     |

## 🖥️ How to Run DevAnalyzer

After downloading the latest release from
the [Releases](https://github.com/Coding-Meet/DevAnalyzer/releases) page, follow the steps based on
your operating system.

### Windows

1. Download the `.msi` installer from the Assets section.
2. Double-click the file and follow the setup instructions.
3. Once installed, you can launch DevAnalyzer from the Start menu.

### macOS

1. Download the `.dmg` file from the Assets section.
2. Double-click it to open, then drag DevAnalyzer to the Applications folder.
3. The first time you open the app, macOS might show a warning:

> “DevAnalyzer can’t be opened because it is from an unidentified developer.”

To fix this:

1. Open **System Settings → Privacy & Security**
2. Scroll down to **Security**
3. Click **Allow Anyway** next to “DevAnalyzer”
4. Reopen the app — it will launch successfully.

> macOS sometimes blocks unsigned apps for security reasons. Once allowed from Privacy & Security,
> the app will work normally.

### Linux (Ubuntu/Debian)

1. Download the `.deb` package from the Assets section.
2. Open a terminal in the download directory and run:
   ```shell
   sudo dpkg -i devanalyzer_1.0.0-1_amd64.deb
   ```
3. After installation, you can launch the app from your system menu or by running:
   ```shell
   devanalyzer
   ```

### Run from Source (Development Mode)

If you want to build and run the app from source:

**macOS/Linux**

```shell
./gradlew :composeApp:run
```

**Windows**

```shell
.\gradlew.bat :composeApp:run
```

---

## Contributing 🤝

Contributions, issues, and feature suggestions are always welcome! 🙌
If you have ideas to make DevAnalyzer better, feel free to open a pull request or start a
discussion.

## ❤ Show your support

Give a ⭐️ if this project helped you!

<a href="https://www.buymeacoffee.com/codingmeet" target="_blank">
<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" width="160">
</a>

Your generosity is greatly appreciated! Thank you for supporting this project.

## Connect with me

[![](https://img.shields.io/badge/Youtube-red?style=for-the-badge&logo=youtube&logoColor=white)](https://youtube.com/@CodingMeet26?si=FuKwU-aBaf_5kukR)
[![](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/coding-meet/)
[![](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/CodingMeet)

## Author

**Meet**

---

## Screenshots

### Onboarding Feature

![Onboarding Screenshot](screenshot/onboarding/img_1.png)
![Onboarding Screenshot](screenshot/onboarding/img_2.png)
![Onboarding Screenshot](screenshot/onboarding/img_3.png)
![Onboarding Screenshot](screenshot/onboarding/img_4.png)
![Onboarding Screenshot](screenshot/onboarding/img_5.png)
![Onboarding Screenshot](screenshot/onboarding/img_6.png)
![Onboarding Screenshot](screenshot/onboarding/img_7.png)
![Onboarding Screenshot](screenshot/onboarding/img_8.png)

### Development Project Analyzer Feature

![Development Project Analyzer Screenshot](screenshot/project/img.png)
![Development Project Analyzer Screenshot](screenshot/project/img_1.png)
![Development Project Analyzer Screenshot](screenshot/project/img_2.png)
![Development Project Analyzer Screenshot](screenshot/project/img_3.png)
![Development Project Analyzer Screenshot](screenshot/project/img_4.png)
![Development Project Analyzer Screenshot](screenshot/project/img_5.png)

### Development Storage Analyzer Feature

![Development Storage Analyzer Screenshot](screenshot/storage/img.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_1.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_2.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_3.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_4.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_5.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_6.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_7.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_8.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_9.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_10.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_11.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_12.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_13.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_14.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_15.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_16.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_17.png)
![Development Storage Analyzer Screenshot](screenshot/storage/img_18.png)
