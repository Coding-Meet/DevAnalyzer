package com.meet.dev.analyzer.data.models.storage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class StorageBreakdown(
    val totalSizeByte: Long,
    val totalSizeReadable: String,
    val storageBreakdownItemList: List<StorageBreakdownItem>,
)

data class StorageBreakdownItem(
    val name: String,
    val sizeByte: Long,
    val sizeReadable: String,
    val percentage: Float = 0.0f,
    val percentageReadable: String = "",
    val storageBreakdownItemColor: StorageBreakdownItemColor,
)

enum class StorageBreakdownItemColor(val color: Color, val icon: ImageVector) {
    GradleCache(Color(0xFF2196F3), Icons.Default.Folder),
    IdeCache(Color(0xFF4CAF50), Icons.Default.Code),
    KotlinNativeInfo(Color(0xFF9C27B0), Icons.Default.Memory),
    Sdk(Color(0xFF3DDC84), Icons.Default.Android),
    Avd(Color(0xFFE91E63), Icons.Default.PhoneAndroid),
    Jdk(Color(0xFFED8B00), Icons.Default.Coffee)
}

