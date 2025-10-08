package com.meet.project.analyzer.presentation.screen.scanner.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.scanner.GradleWrapperPropertiesFileInfo
import com.meet.project.analyzer.data.models.scanner.ModuleBuildFileInfo
import com.meet.project.analyzer.data.models.scanner.PropertiesFileInfo
import com.meet.project.analyzer.data.models.scanner.SettingsGradleFileInfo
import com.meet.project.analyzer.data.models.scanner.VersionCatalogFileInfo
import com.meet.project.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import java.awt.Cursor
import java.io.File

@Composable
fun BuildFilesTabContent(
    projectName: String,
    moduleBuildFileInfos: List<ModuleBuildFileInfo>,
    settingsGradleFileInfo: SettingsGradleFileInfo?,
    propertiesFileInfo: PropertiesFileInfo?,
    gradleWrapperPropertiesFileInfo: GradleWrapperPropertiesFileInfo?,
    versionCatalogFileInfo: VersionCatalogFileInfo?,
) {
    var selectedBuildFile by remember { mutableStateOf<BuildFile?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left panel - File tree
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            val scrollState = rememberLazyListState()
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = moduleBuildFileInfos,
                        key = { it.uniqueId }
                    ) { moduleBuildFileInfo ->
                        DetailedBuildFileCard(
                            selectedPath = selectedBuildFile?.path,
                            type = moduleBuildFileInfo.type.name,
                            moduleName = moduleBuildFileInfo.moduleName,
                            size = moduleBuildFileInfo.size,
                            fileName = moduleBuildFileInfo.type.fileName,
                            path = moduleBuildFileInfo.path,
                            file = moduleBuildFileInfo.file,
                            content = moduleBuildFileInfo.content
                        ) {
                            selectedBuildFile = it
                        }
                    }

                    if (settingsGradleFileInfo != null) {
                        item(
                            key = settingsGradleFileInfo.uniqueId
                        ) {
                            DetailedBuildFileCard(
                                selectedPath = selectedBuildFile?.path,
                                type = settingsGradleFileInfo.type.name,
                                moduleName = projectName,
                                size = settingsGradleFileInfo.size,
                                fileName = settingsGradleFileInfo.type.fileName,
                                path = settingsGradleFileInfo.path,
                                file = settingsGradleFileInfo.file,
                                content = settingsGradleFileInfo.content
                            ) {
                                selectedBuildFile = it

                            }
                        }
                    }
                    if (propertiesFileInfo != null) {
                        item(
                            key = propertiesFileInfo.uniqueId
                        ) {
                            DetailedBuildFileCard(
                                selectedPath = selectedBuildFile?.path,
                                type = propertiesFileInfo.type.name,
                                moduleName = projectName,
                                size = propertiesFileInfo.size,
                                fileName = propertiesFileInfo.type.fileName,
                                path = propertiesFileInfo.path,
                                file = propertiesFileInfo.file,
                                content = propertiesFileInfo.content
                            ) {
                                selectedBuildFile = it

                            }
                        }
                    }
                    if (gradleWrapperPropertiesFileInfo != null) {
                        item(
                            key = gradleWrapperPropertiesFileInfo.uniqueId
                        ) {
                            DetailedBuildFileCard(
                                selectedPath = selectedBuildFile?.path,
                                type = "properties",
                                moduleName = projectName,
                                size = gradleWrapperPropertiesFileInfo.size,
                                fileName = gradleWrapperPropertiesFileInfo.name,
                                path = gradleWrapperPropertiesFileInfo.path,
                                file = gradleWrapperPropertiesFileInfo.file,
                                content = gradleWrapperPropertiesFileInfo.content
                            ) {
                                selectedBuildFile = it

                            }
                        }
                    }
                    if (versionCatalogFileInfo != null) {
                        item(
                            key = versionCatalogFileInfo.uniqueId
                        ) {
                            DetailedBuildFileCard(
                                selectedPath = selectedBuildFile?.path,
                                type = versionCatalogFileInfo.name,
                                moduleName = projectName,
                                size = versionCatalogFileInfo.size,
                                fileName = versionCatalogFileInfo.name,
                                path = versionCatalogFileInfo.path,
                                file = versionCatalogFileInfo.file,
                                content = versionCatalogFileInfo.content
                            ) {
                                selectedBuildFile = it

                            }
                        }
                    }

                    if (moduleBuildFileInfos.isEmpty() &&
                        settingsGradleFileInfo == null &&
                        propertiesFileInfo == null &&
                        gradleWrapperPropertiesFileInfo == null &&
                        versionCatalogFileInfo == null
                    ) {
                        item {
                            EmptyStateCardLayout(
                                "No build files found",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                }
                VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
            }
        }

        // Divider
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        // Right panel - File content
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            if (selectedBuildFile != null) {
                BuildFileContentViewer(selectedBuildFile!!)
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Select a file to view its content",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BuildFileContentViewer(
    buildFile: BuildFile
) {
    val scrollState = rememberScrollState()

    // File content
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // File header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getBuildFileIcon(buildFile.type),
                    contentDescription = buildFile.fileName,
                    tint = getBuildFileColor(buildFile.type),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        buildFile.fileName.replaceFirstChar { it.uppercase() }
                                + " (:" + buildFile.moduleName.replaceFirstChar { it.uppercase() } + ")",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        buildFile.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                            .clickable {
                                buildFile.file.openFile()
                            }
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        buildFile.size,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 1.dp
                ) {
                    SelectionContainer {
                        Text(
                            buildFile.content,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }
            }
            VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
        }
    }

}

data class BuildFile(
    val type: String,
    val moduleName: String,
    val size: String,
    val fileName: String,
    val path: String,
    val file: File,
    val content: String,
)

@Composable
fun DetailedBuildFileCard(
    selectedPath: String?,
    type: String,
    moduleName: String,
    size: String,
    fileName: String,
    path: String,
    file: File,
    content: String,
    openBuildFile: (BuildFile) -> Unit = {},
) {
    val isSelected by rememberSaveable(selectedPath, path) {
        derivedStateOf {
            selectedPath == path
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth().pointerHoverIcon(
            PointerIcon(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        onClick = {
            openBuildFile(
                BuildFile(
                    type = type,
                    moduleName = moduleName,
                    size = size,
                    fileName = fileName,
                    path = path,
                    file = file,
                    content = content
                )
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        // Build file header
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getBuildFileIcon(type),
                contentDescription = null,
                tint = getBuildFileColor(type),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    fileName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getBuildFileColor(type)
                )
                Text(
                    "(:" + moduleName.replaceFirstChar { it.uppercase() } + ")",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

