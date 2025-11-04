# 🧠 DevAnalyzer

[![DevAnalyzer](screenshot/dev_analyzer.png)]()

---

## 🪄 Overview

**DevAnalyzer** is a cross-platform desktop application built with **Compose Multiplatform** and *
*Kotlin Multiplatform (KMP)**.  
It provides deep insights into your **Android/Kotlin projects** and **local development environment
** through two major modules:

- 🧩 **Project Analyzer** — Inspects project modules, Gradle configurations, plugins, dependencies
  and related project files.
- 💾 **Storage Analyzer** — Inspects SDKs, IDEs, Gradle caches, and related storage usage on your
  machine.

This tool helps developers **understand**, **analyze**, and **optimize** their development
ecosystem — all from a single unified interface.

This desktop-first tool runs seamlessly across platforms and can easily extend to Android, KMP, or
backend Kotlin targets.

---

## 🚀 Features

### 🧩 Project Analyzer

- 🔍 Analyze **Gradle modules**, **plugins**, and **dependencies**.
- 📦 List all applied plugins and version catalogs.
- 📄 View **build files** and configuration scripts in an organized manner.
- 🧱 Inspect project metadata such as Gradle Kotlin, AGP, Min SDK, Compile SDK, Target SDK and
  Multi-Module.
- 🧾 Preview all **project and Gradle files** directly in the app.

### 💾 Storage Analyzer

- 💡 Get total storage summaries by component (SDK, IDE, Gradle, Library, etc.).
- 📊 Scan **SDK**, **NDK**, **CMake**, **Kotlin/Native**, **JDK** and **Extras** directories.
- 📄 Analyze **IDE data** (Android Studio, IntelliJ) including logs, caches, and support files.
- 🧠 Inspect **Gradle Daemons**, **Wrappers**, and **Cached Libraries**.

---

## 🧰 Tech Stack

| Category | Libraries & Tools                                                       |
|-----------|-------------------------------------------------------------------------|
| **Framework** | Compose Multiplatform |
| **Language** | Kotlin 2.x (Multiplatform)                                              |
| **Architecture** | MVVM |
| **Design System** | Material 3 with Adaptive Navigation Suite                               |
| **Dependency Injection** | Koin                                             |
| **Navigation** | Jetpack Navigation for Compose                                          |
| **Image Loading** | Coil3 (Compose + Ktor + Multiplatform)        |
| **Local Storage** | DataStore (Core + Preferences)                                          |
| **Serialization** | kotlinx.serialization                                              |
| **Logging** | Kermit (TouchLab Multiplatform Logger)                                  |
| **File Handling** | FileKit (Dialogs + Compose)                                             |
| **Theme Detection** | JSystemThemeDetector                                                    |
| **Semantic Versioning** | SemVer (z4kn4fein/semver)                                               |
| **Coroutines** | kotlinx.coroutines + Swing Dispatcher (Desktop)                         |

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
