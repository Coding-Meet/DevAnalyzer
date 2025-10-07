package com.meet.project.analyzer.presentation.screen.scanner.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.meet.project.analyzer.data.models.scanner.BuildFileType
import com.meet.project.analyzer.data.models.scanner.FileType
import com.meet.project.analyzer.data.models.scanner.PropertiesFileType
import com.meet.project.analyzer.data.models.scanner.SettingsGradleFileType

fun getDependencyTypeColor(type: String): Color {
    return when (type) {
        "implementation" -> Color(0xFF4CAF50)
        "api" -> Color(0xFF2196F3)
        "compileOnly" -> Color(0xFFFF9800)
        "runtimeOnly" -> Color(0xFF9C27B0)
        "testImplementation" -> Color(0xFFE91E63)
        "androidTestImplementation" -> Color(0xffffb114)
        "kapt" -> Color(0xFF795548)
        "ksp" -> Color(0xFF607D8B)
        "plugin" -> Color(0xFF00BCD4)
        "normal" -> Color(0xFF8BC34A)
        "classpath" -> Color(0xFFFFC107)
        "versionCatalog" -> Color(0xff8acea9)
        else -> Color.Gray
    }

}

fun getBuildFileIcon(type: String): ImageVector {
    return when (type) {
        BuildFileType.BUILD_GRADLE_KTS.name, BuildFileType.BUILD_GRADLE.name -> Icons.Default.Build
        SettingsGradleFileType.SETTINGS_GRADLE_KTS.name, SettingsGradleFileType.SETTINGS_GRADLE.name -> Icons.Default.Settings
        PropertiesFileType.GRADLE_PROPERTIES.name, PropertiesFileType.LOCAL_PROPERTIES.name -> Icons.Default.Settings
        "properties" -> Icons.Default.Settings
        "libs.versions.toml" -> Icons.Default.Build
        else -> Icons.Default.BookmarkBorder
    }
}

fun getBuildFileColor(type: String): Color {
    return when (type) {
        BuildFileType.BUILD_GRADLE_KTS.name,
        BuildFileType.BUILD_GRADLE.name -> Color(0xFF2196F3)

        SettingsGradleFileType.SETTINGS_GRADLE_KTS.name,
        SettingsGradleFileType.SETTINGS_GRADLE.name -> Color(0xFF607D8B)

        PropertiesFileType.GRADLE_PROPERTIES.name,
        PropertiesFileType.LOCAL_PROPERTIES.name -> Color(0xFF795548)

        "properties" -> Color(0xFF795548)
        "libs.versions.toml" -> Color(0xFF2196F3)
        else -> Color(0xFF7C4DFF)
    }
}

@Composable
fun getModuleColor(moduleName: String): Color {
    return if (moduleName == "root") {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
}

fun getModuleIcon(moduleName: String): ImageVector {
    return if (moduleName == "root") {
        Icons.Default.Extension
    } else {
        Icons.Default.Folder
    }
}

fun getFileTypeIcon(type: FileType): ImageVector {
    return when (type) {
        FileType.SOURCE_KOTLIN -> Icons.Default.Code
        FileType.SOURCE_JAVA -> Icons.Default.Coffee
        FileType.BUILD_SCRIPT -> Icons.Default.Build
        FileType.CONFIGURATION -> Icons.Default.Settings
        FileType.RESOURCE -> Icons.Default.Image
        FileType.MANIFEST -> Icons.Default.Description
        FileType.LAYOUT -> Icons.AutoMirrored.Filled.ViewQuilt
        FileType.DRAWABLE -> Icons.Default.Image
        FileType.VALUES -> Icons.AutoMirrored.Filled.List
        FileType.ASSETS -> Icons.Default.Folder
        FileType.PROPERTIES -> Icons.Default.Settings
        FileType.JSON -> Icons.Default.DataObject
        FileType.XML -> Icons.Default.Code
        FileType.TEXT -> Icons.Default.TextFields
        FileType.MARKDOWN -> Icons.AutoMirrored.Filled.Article
        FileType.IMAGE -> Icons.Default.Image
        FileType.OTHER -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

fun getFileTypeColor(type: FileType): Color {
    return when (type) {
        FileType.SOURCE_KOTLIN -> Color(0xFF7F52FF)
        FileType.SOURCE_JAVA -> Color(0xFFED8B00)
        FileType.BUILD_SCRIPT -> Color(0xFF02303A)
        FileType.CONFIGURATION -> Color(0xFF607D8B)
        FileType.RESOURCE -> Color(0xFF4CAF50)
        FileType.MANIFEST -> Color(0xFF3DDC84)
        FileType.LAYOUT -> Color(0xFF2196F3)
        FileType.DRAWABLE -> Color(0xFF9C27B0)
        FileType.VALUES -> Color(0xFFFF9800)
        FileType.ASSETS -> Color(0xFF795548)
        FileType.PROPERTIES -> Color(0xFF607D8B)
        FileType.JSON -> Color(0xFF4CAF50)
        FileType.XML -> Color(0xFFFF5722)
        FileType.TEXT -> Color(0xFF9E9E9E)
        FileType.MARKDOWN -> Color(0xFF673AB7)
        FileType.IMAGE -> Color(0xFF2196F3)
        FileType.OTHER -> Color(0xFF9E9E9E)
    }
}