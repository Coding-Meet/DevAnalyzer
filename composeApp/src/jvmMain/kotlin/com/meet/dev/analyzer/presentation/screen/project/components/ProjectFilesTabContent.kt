package com.meet.dev.analyzer.presentation.screen.project.components

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.meet.dev.analyzer.core.utility.Utils.openFile
import com.meet.dev.analyzer.data.models.project.FileType
import com.meet.dev.analyzer.data.models.project.ProjectFileInfo
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import java.awt.Cursor
import java.io.File

@Composable
fun ProjectFilesTabContent(projectFiles: List<ProjectFileInfo>, projectName: String) {
    var selectedFile by remember { mutableStateOf<ProjectFileInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFiles by remember(searchQuery, projectFiles) {
        derivedStateOf {
            if (searchQuery.isBlank()) projectFiles
            else projectFiles.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.relativePath.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val fileTree by remember(filteredFiles) {
        derivedStateOf {
            filteredFiles.groupBy { file ->
                val pathParts = file.relativePath.split("/", "\\")
                if (pathParts.size > 1) {
                    pathParts.dropLast(1).joinToString("/")
                } else {
                    projectName
                }
            }.toSortedMap()
        }
    }
    Row(modifier = Modifier.fillMaxSize()) {
        // Left panel - File tree
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            val scrollState = rememberLazyListState()

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Column {
                    // Search Field
                    CustomOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        onClear = { searchQuery = "" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        leadingIcon = Icons.Default.Search,
                        labelText = "Search files..."
                    )
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        if (filteredFiles.isNotEmpty()) {

                            item {
                                Text(
                                    "Project Files (${filteredFiles.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            // Build file tree structure
                            fileTree.forEach { (folder, projectFileInfoList) ->
                                item(
                                    key = folder
                                ) {
                                    FolderHeader(
                                        folderPath = folder,
                                        fileCount = projectFileInfoList.size
                                    )
                                }
                                items(
                                    items = projectFileInfoList,
                                    key = { projectFileInfo ->
                                        projectFileInfo.uniqueId
                                    }) { projectFileInfo ->
                                    FileTreeItem(
                                        projectFileInfo = projectFileInfo,
                                        isSelected = selectedFile == projectFileInfo,
                                        onClick = { selectedFile = projectFileInfo }
                                    )
                                }
                            }
                        } else {
                            item {
                                EmptyStateCardLayout(
                                    title = "Project files",
                                    description = if (searchQuery.isBlank()) "No files found"
                                    else "No results for \"$searchQuery\"",
                                    icon = Icons.Default.FileOpen
                                )
                            }
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
            if (selectedFile != null) {
                FileContentViewer(selectedFile!!)
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
fun FileContentViewer(projectFileInfo: ProjectFileInfo) {
    val scrollState = rememberScrollState()

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
                    getFileTypeIcon(projectFileInfo.type),
                    contentDescription = null,
                    tint = getFileTypeColor(projectFileInfo.type),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        projectFileInfo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        projectFileInfo.relativePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                            .clickable {
                                projectFileInfo.file.openFile()
                            }
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = projectFileInfo.sizeReadable,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // File content
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            if (projectFileInfo.type == FileType.IMAGE) {
                // Show image preview
                AsyncImage(
                    model = File(projectFileInfo.path),
                    contentDescription = projectFileInfo.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else if (projectFileInfo.isReadable && projectFileInfo.content != null) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                ) {
                    SelectionContainer {
                        Text(
                            projectFileInfo.content,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }

                VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
            } else {
                // Non-readable file
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (projectFileInfo.content == null) "Binary file - cannot display content" else "File too large to display",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "File type: ${projectFileInfo.type.name.replace("_", " ").lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun FolderHeader(folderPath: String, fileCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                folderPath,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Text(
                fileCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileTreeItem(
    projectFileInfo: ProjectFileInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            Color.Transparent,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(MaterialTheme.shapes.small)
            .pointerHoverIcon(
                PointerIcon(
                    Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                    )
                )
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                getFileTypeIcon(projectFileInfo.type),
                contentDescription = null,
                tint = getFileTypeColor(projectFileInfo.type),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                projectFileInfo.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                projectFileInfo.sizeReadable,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

