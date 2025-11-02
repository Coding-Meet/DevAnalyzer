package com.meet.project.analyzer.core.utility

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

interface ExpandableSection {
    val title: String
    val description: String
    val totalLabel: String
    val messageTitle: String
    val messageDescription: String
    val cardColors: @Composable () -> CardColors
    val titleColor: @Composable () -> Color
    val valueColor: @Composable () -> Color
    val labelColor: @Composable () -> Color
    val icon: ImageVector
}

enum class StorageSummarySection(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onPrimaryContainer },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector,
) : ExpandableSection {

    TotalSummary(
        title = "Total Storage Used",
        description = "Displays total storage used and category-wise breakdown including IDE, SDK, AVD, Gradle, and libraries.",
        totalLabel = "Total Storage",
        messageTitle = "No Storage Summary Data.",
        messageDescription = "Storage summary details are not available or failed to load.",
        icon = Icons.Default.Storage
    )
}

enum class IdeDataSection(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector
) : ExpandableSection {

    IdeSummary(
        title = "IDE Summary",
        description = "Shows total IDE installations, caches, logs, and support data with combined storage usage.",
        totalLabel = "Total IDE",
        messageTitle = "No IDE Summary Data.",
        messageDescription = "IDE summary information is not available or not scanned yet.",
        icon = Icons.Default.Assessment
    ),

    MacCaches(
        title = "Caches",
        description = "Lists IDE cache files such as Android Studio and IntelliJ caches stored in user Library folders.",
        totalLabel = "Total Cache",
        messageTitle = "No IDE Cache Data.",
        messageDescription = "IDE cache files were not found or not scanned.",
        icon = Icons.Default.Storage
    ),

    MacLogs(
        title = "Logs",
        description = "Displays IDE log files for each installed version.",
        totalLabel = "Total Logs",
        messageTitle = "No IDE Logs Data.",
        messageDescription = "IDE log data is missing or not collected.",
        icon = Icons.AutoMirrored.Filled.Article
    ),

    MacSupport(
        title = "Support",
        description = "Includes IDE-related files from Application Support directories.",
        totalLabel = "Total Support",
        messageTitle = "No IDE Support Data.",
        messageDescription = "IDE support information could not be loaded.",
        icon = Icons.Default.Folder
    ),

    WinProgramFiles(
        title = "Program Files",
        description = "Shows IDE installations inside Windows Program Files directories.",
        totalLabel = "Total Program Files",
        messageTitle = "No IDE Program Files Data.",
        messageDescription = "Windows IDE installation data not detected.",
        icon = Icons.Default.Apps
    ),

    WinLocal(
        title = "Local",
        description = "Displays IDE data stored under AppData/Local on Windows.",
        totalLabel = "Total Local",
        messageTitle = "No Local IDE Data.",
        messageDescription = "AppData/Local IDE information is missing or unavailable.",
        icon = Icons.Default.FolderOpen
    ),

    WinRoaming(
        title = "Roaming",
        description = "Lists IDE-related files from AppData/Roaming on Windows.",
        totalLabel = "Total Roaming",
        messageTitle = "No Roaming IDE Data.",
        messageDescription = "AppData/Roaming IDE details not available.",
        icon = Icons.Default.Cloud
    )
}

enum class AvdSystemImageSection(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector,
) : ExpandableSection {

    AvdDevices(
        title = "AVD Devices",
        description = "Lists configured Android Virtual Devices with their allocated storage sizes and file paths.",
        totalLabel = "Total AVD",
        messageTitle = "No AVD Device Data.",
        messageDescription = "No Android Virtual Devices were found or loaded.",
        icon = Icons.Default.PhoneAndroid
    ),

    SystemImages(
        title = "System Images",
        description = "Displays downloaded Android system images used by AVDs.",
        totalLabel = "Total System Images",
        messageTitle = "No System Image Data.",
        messageDescription = "No Android system images were detected or loaded.",
        icon = Icons.Default.Image
    )
}

enum class AndroidSdkSection(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector,
) : ExpandableSection {

    SdkPlatforms(
        title = "SDK Platforms",
        description = "Displays installed Android SDK platform versions with their respective API levels and sizes.",
        totalLabel = "Total SDK Platforms",
        messageTitle = "No SDK Platform Data.",
        messageDescription = "No Android SDK platforms found or failed to load.",
        icon = Icons.Default.Android
    ),

    BuildTools(
        title = "Build Tools",
        description = "Lists all installed build tool versions used by Gradle builds.",
        totalLabel = "Total Build Tools",
        messageTitle = "No Build Tools Data.",
        messageDescription = "Android build tools are not installed or not detected.",
        icon = Icons.Default.Build
    ),

    Sources(
        title = "Sources",
        description = "Shows installed Android SDK sources for specific API levels.",
        totalLabel = "Total Sources",
        messageTitle = "No Sources Data.",
        messageDescription = "No SDK sources were found on this system.",
        icon = Icons.Default.Code
    ),

    NdkVersions(
        title = "NDK Versions",
        description = "Lists installed Native Development Kit versions for native builds.",
        totalLabel = "Total NDK Versions",
        messageTitle = "No NDK Data.",
        messageDescription = "Native Development Kit versions not installed or unavailable.",
        icon = Icons.Default.Memory
    ),

    Cmake(
        title = "CMake",
        description = "Displays installed CMake versions for native C/C++ build configurations.",
        totalLabel = "Total CMake Versions",
        messageTitle = "No CMake Data.",
        messageDescription = "CMake installations were not found.",
        icon = Icons.Default.Settings
    ),

    Extras(
        title = "Extras",
        description = "Includes additional SDK components like emulator, platform-tools, and other utilities.",
        totalLabel = "Total Extras",
        messageTitle = "No Extras Data.",
        messageDescription = "No additional SDK components detected or loaded.",
        icon = Icons.Default.Folder
    )
}

enum class KotlinNativeJdkSection(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector,
) : ExpandableSection {

    JdkVersions(
        title = "JDK Versions",
        description = "Lists all installed Java Development Kit versions along with their paths and sizes.",
        totalLabel = "Total JDK Versions",
        messageTitle = "No JDK Data.",
        messageDescription = "Java Development Kit information is not available or not installed.",
        icon = Icons.Default.Code
    ),

    KotlinNative(
        title = "Kotlin/Native",
        description = "Displays installed Kotlin/Native toolchains and prebuilt versions with their sizes.",
        totalLabel = "Total Kotlin/Native",
        messageTitle = "No Kotlin/Native Dependencies Data.",
        messageDescription = "Kotlin/Native Dependencies information not loaded.",
        icon = Icons.Default.DataObject
    ),

    LldbVersions(
        title = "LLVM/LLDB Versions",
        description = "Shows installed LLVM and LLDB builds used by Kotlin/Native toolchains.",
        totalLabel = "Total LLVM/LLDB Versions",
        messageTitle = "No LLVM/LLDB Data.",
        messageDescription = "LLVM/LLDB dependencies not found or failed to load.",
        icon = Icons.Default.Settings
    )
}

enum class GradleSection(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector,
) : ExpandableSection {

    GradleWrapper(
        title = "Gradle Wrapper",
        description = "Lists all Gradle wrapper versions installed with their paths and storage usage.",
        totalLabel = "Total Wrapper",
        messageTitle = "No Gradle Wrapper Data.",
        messageDescription = "Gradle wrapper versions not found or not installed.",
        icon = Icons.Default.Build
    ),

    GradleDaemon(
        title = "Gradle Daemon",
        description = "Displays Gradle daemon versions and active instances consuming disk space.",
        totalLabel = "Total Daemon",
        messageTitle = "No Gradle Daemon Data.",
        messageDescription = "Gradle daemon information unavailable or not loaded.",
        icon = Icons.Default.Memory
    ),

    GradleCaches(
        title = "Gradle Caches",
        description = "Shows all cached Gradle versions and their total storage usage.",
        totalLabel = "Total Caches",
        messageTitle = "No Gradle Cache Data.",
        messageDescription = "Gradle cache directories are empty or not scanned.",
        icon = Icons.Default.Storage
    ),

    OtherGradle(
        title = "Other Gradle Folders",
        description = "Includes additional Gradle-related directories like build-cache, transforms, and jars.",
        totalLabel = "Total Other Gradle",
        messageTitle = "No Additional Gradle Data.",
        messageDescription = "Other Gradle folders not detected or empty.",
        icon = Icons.Default.Folder
    )
}

enum class GradleLibrary(
    override val title: String,
    override val description: String,
    override val totalLabel: String,
    override val messageTitle: String,
    override val messageDescription: String,
    override val cardColors: @Composable () -> CardColors = { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) },
    override val titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
    override val valueColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    override val labelColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurfaceVariant },
    override val icon: ImageVector,
) : ExpandableSection {

    Libraries(
        title = "Gradle Libraries",
        description = "Displays all cached libraries from Gradle with group, name, version, and total size.",
        totalLabel = "Total Libraries",
        messageTitle = "No Library Data.",
        messageDescription = "Gradle libraries are not downloaded or unavailable.",
        icon = Icons.AutoMirrored.Filled.LibraryBooks
    )
}
