# 🧠 DevAnalyzer

### Cross-Platform Development Analyzer Suite (Compose Multiplatform Desktop)

---

## 📖 Overview

**DevAnalyzer** is a **Compose Multiplatform Desktop App** built with **Kotlin Multiplatform (KMP)**
that helps developers analyze their entire development environment. It brings deep insights into
both **project structure** and **system storage** used by Android, Kotlin, and backend development
setups.

It includes two core modules:

* **Project Analyzer** → Scans and analyzes project configuration, Gradle modules, plugins, and
  dependencies.
* **Storage Analyzer** → Inspects SDKs, IDEs, Gradle caches, and related storage usage on your
  machine.

This desktop-first tool runs seamlessly across platforms and can easily extend to Android, KMP, or
backend Kotlin targets.

---

## ⚙️ Features

### 🧩 **Project Analyzer**

Gain full insight into your project structure and Gradle configuration.

* 🔍 Analyze Gradle modules, dependencies, and build scripts.
* 📦 List all applied plugins and version catalogs.
* 🧱 Inspect configuration data (Min SDK, Target SDK, Kotlin/AGP versions).
* 🧾 Browse and preview Gradle and project files directly.

### 💾 **Storage Analyzer**

Visualize your development storage usage across tools.

* 📊 Scan SDKs, IDE data, NDK, CMake, and Extras.
* 🧠 Inspect Gradle daemons, wrappers, and caches.
* 🧩 Analyze Kotlin/Native, LLVM, and JDK installations.
* 💡 Identify heavy directories and potential cleanup targets.

---

## 🧭 **Navigation Structure**

| Section             | Purpose                                                        |
|---------------------|----------------------------------------------------------------|
| 🔍 **Project**      | Analyze project modules, dependencies, and Gradle build setup. |
| 💾 **Storage**      | Analyze IDE, SDK, and Gradle storage usage.                    |
| 🌞 **Theme Switch** | Toggle light/dark theme from the sidebar footer.               |

## 🧰 **Tech Stack**

| Layer                | Technology                                       |
|----------------------|--------------------------------------------------|
| **UI**               | Compose Multiplatform (Desktop, Android)         |
| **Language**         | Kotlin 2.x (Multiplatform)                       |
| **Architecture**     | Modular Analyzer System (Enum-based UI Model)    |
| **Design System**    | Material 3 (Compose for Desktop)                 |
| **Build Tool**       | Gradle KMP DSL                                   |
| **Platform Support** | Desktop ✅ · Android ✅ · KMP ✅ · Backend Kotlin ✅ |

---

## 🧠 **Key Highlights**

* 🧩 Compose Multiplatform Desktop UI using Material 3.
* ⚙️ Works across JVM and native KMP targets.
* 📊 Unified analysis for both project and environment.
* 💡 Lightweight, modular, and easy to extend.
* 🧱 Enum-driven architecture for consistent, dynamic UI.

---

## 💻 **Setup & Run Instructions**

### 🧩 **Requirements**

* Kotlin 2.x or later
* Gradle 8.x+
* JDK 17+
* Compose Multiplatform plugin enabled in IDE (IntelliJ IDEA recommended)

### ▶️ **Run the Desktop App**

```bash
git clone https://github.com/Coding-Meet/DevAnalyzer.git
cd DevAnalyzer
./gradlew run
```

### 🧪 **Build Executable (Desktop)**

```bash
./gradlew packageDistributionForCurrentOS
```

This will generate a platform-specific build under `build/compose/binaries`.

---

## 🧩 **Future Enhancements**

* 🧹 Environment cleanup tools (safe Gradle/IDE cache removal)
* 📊 Interactive graphs for dependencies and storage usage
* 🔄 Real-time analysis & background scanning
* ☁️ Sync analyzer reports with CodingMeet Cloud
* 🧱 Plugin API for custom analyzers

---

## 🏷️ **Project Info**

* **Name:** DevAnalyzer
* **Version:** 1.0.0 (Beta)
* **Developer:** Meet Bhavsar ([Coding Meet](https://codingmeet.com))
* **Type:** Compose Multiplatform Desktop App
* **Website:** [codingmeet.com/devanalyzer](https://codingmeet.com)
* **License:** MIT

---

### 🧡 **Developed with passion by [Coding Meet](https://codingmeet.com)**

# Project Analyzer

Project Analyzer is a powerful tool for Android developers to gain insights into their projects and
manage their development environment's storage. It provides a detailed breakdown of your project's
structure and analyzes storage consumption by various components like IDEs, SDKs, Gradle, and more.

## Features

### Project Analysis

- **Overview**: Get a high-level overview of your project.
- **Modules**: Explore the different modules in your project.
- **Plugins**: See a list of all plugins used in the project.
- **Dependencies**: Analyze the project's dependencies.
- **Build Files**: Inspect the build files of the project.
- **Project Files**: Browse through the project files.

🧠 Development Storage Analyzer

📖 Overview

Development Storage Analyzer is a powerful tool designed to scan, analyze, and visualize storage
usage across key components of your Android development environment.
It helps developers understand how much space is consumed by SDKs, IDEs, Gradle, AVDs,
Kotlin/Native, JDKs, and cached libraries — all in one organized dashboard.

This feature provides a clear, expandable view of storage data with summaries, totals, and
categorized insights, helping developers clean up or optimize their workspace effectively.

⚡ Key Highlights

- 🔍 Deep Environment Scanning – Analyzes Android Studio, IntelliJ, SDK, AVD, Gradle, and toolchain
  folders.
- 📊 Categorized Insights – Displays detailed breakdowns per tool (IDE, SDK, Gradle, Libraries,
  etc.).
- 💾 Readable Storage Summaries – Converts raw sizes into human-friendly units (e.g., MB, GB).
- 🧩 Expandable Sections – Every category includes a collapsible section showing totals and detailed
  file paths.
- ⚙️ Cross-Platform Support – Works across macOS, Windows, and Linux development environments.
- 🧱 Built for Developers – Provides direct paths and real folder structures for informed cleanup or
  debugging.
- 🎨 Material 3 UI + Compose Design – Clean, modern interface consistent with Android development
  tools.

🧭 Tab Overview

| Tab                 | Description                                                                                 |
|---------------------|---------------------------------------------------------------------------------------------|
| Overview            | Displays total storage usage and category-wise breakdown (IDE, SDK, Gradle, etc.)           |
| IDE                 | Analyzes IDE installations (Android Studio, IntelliJ) with caches, logs, and support files. |
| AVD & System Images | Lists configured Android Virtual Devices and downloaded system images.                      |
| Android SDK         | Breaks down SDK Platforms, Build Tools, NDK, CMake, Sources, and Extras.                    |
| Kotlin/Native & JDK | Displays installed JDK versions, Kotlin/Native toolchains, and LLVM/LLDB dependencies.      |
| Gradle              | Analyzes Gradle Daemons, Wrappers, Caches, and additional Gradle directories.               |
| Libraries           | Lists downloaded Gradle libraries with versions, group names, and storage usage.            |
