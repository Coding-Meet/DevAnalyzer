package com.meet.project.analyzer.presentation.screen.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.StorageAnalyzerTabs
import com.meet.project.analyzer.presentation.components.ErrorLayout
import com.meet.project.analyzer.presentation.components.TabLayout
import com.meet.project.analyzer.presentation.components.TabSlideAnimation
import com.meet.project.analyzer.presentation.components.TopAppBar
import com.meet.project.analyzer.presentation.screen.storage.components.AvdsTabContent
import com.meet.project.analyzer.presentation.screen.storage.components.ChartsTabContent
import com.meet.project.analyzer.presentation.screen.storage.components.DevEnvironmentTabContent
import com.meet.project.analyzer.presentation.screen.storage.components.GradleCachesTabContent
import com.meet.project.analyzer.presentation.screen.storage.components.LibrariesTabContent
import com.meet.project.analyzer.presentation.screen.storage.components.OverviewTabContent
import com.meet.project.analyzer.presentation.screen.storage.components.SdkTabContent
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor
import java.awt.datatransfer.StringSelection

@Composable
fun StorageAnalyzerScreen() {
    val viewModel = koinViewModel<StorageAnalyzerViewModel>()
    val uiState by viewModel.uiState.collectAsState()

//    DataClassTOJson(
//        uiState
//    )
    StorageAnalyzerContent(
        uiState = uiState,
        onRefresh = { viewModel.handleIntent(StorageAnalyzerIntent.RefreshData) },
        onLoadAvds = { viewModel.handleIntent(StorageAnalyzerIntent.LoadAvds) },
        onLoadSdk = { viewModel.handleIntent(StorageAnalyzerIntent.LoadSdkInfo) },
        onLoadDevEnv = { viewModel.handleIntent(StorageAnalyzerIntent.LoadDevEnvironment) },
        onLoadGradleCaches = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleCaches) },
        onLoadGradleModules = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleModules) },
        onTabSelected = { previousTabIndex, currentTabIndex, storageAnalyzerTabs ->
            viewModel.handleIntent(
                StorageAnalyzerIntent.SelectTab(
                    previousTabIndex, currentTabIndex, storageAnalyzerTabs
                )
            )
        },
        onClearError = {
            viewModel.handleIntent(StorageAnalyzerIntent.ClearError)
        }
    )
}

@Composable
fun StorageAnalyzerContent(
    uiState: StorageAnalyzerUiState,
    onRefresh: () -> Unit,
    onLoadAvds: () -> Unit,
    onLoadSdk: () -> Unit,
    onLoadDevEnv: () -> Unit,
    onLoadGradleCaches: () -> Unit,
    onLoadGradleModules: () -> Unit,
    onTabSelected: (previousTabIndex: Int, currentTabIndex: Int, storageAnalyzerTabs: StorageAnalyzerTabs) -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Storage Analyzer - Top App Bar
            TopAppBar(
                title = "Storage Analyzer", icon = Icons.Default.Storage, actions = {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(
                            PointerIcon(
                                Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                                )
                            )
                        ), onClick = onRefresh
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                        )
                    }
                }
            )
        }
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(it)
        ) {
            // Loading indicator
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary
                )
            }

            // Error Layout
            if (uiState.error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                    ) {
                        ErrorLayout(error = uiState.error, onClearError = onClearError)
                    }
                }
            }

            // Tab Row
            TabLayout(
                selectedTabIndex = uiState.selectedTabIndex,
                tabList = StorageAnalyzerTabs.entries,
                onClick = onTabSelected
            )

            // Tab Content with Scrollbar
            TabSlideAnimation(
                selectedTabIndex = uiState.selectedTabIndex,
                previousTabIndex = uiState.previousTabIndex,
                targetState = uiState.selectedTab
            ) { selectedTab ->

                when (selectedTab) {
                    StorageAnalyzerTabs.Overview -> {
                        OverviewTabContent(uiState = uiState)
                    }

                    StorageAnalyzerTabs.AVDs -> {
                        AvdsTabContent(uiState = uiState, onLoadAvds = onLoadAvds)
                    }

                    StorageAnalyzerTabs.SDK -> {
                        SdkTabContent(uiState = uiState, onLoadSdk = onLoadSdk)
                    }

                    StorageAnalyzerTabs.Environment -> {
                        DevEnvironmentTabContent(uiState = uiState, onLoadDevEnv = onLoadDevEnv)
                    }

                    StorageAnalyzerTabs.Caches -> {
                        GradleCachesTabContent(
                            uiState = uiState,
                            onLoadGradleCaches = onLoadGradleCaches
                        )
                    }

                    StorageAnalyzerTabs.Libraries -> {
                        LibrariesTabContent(
                            uiState = uiState,
                            onLoadGradleModules = onLoadGradleModules
                        )
                    }

                    StorageAnalyzerTabs.Charts -> {
                        ChartsTabContent(uiState = uiState)
                    }
                }
            }
        }
    }
}

@Composable
private fun DataClassTOJson(
    uiState: StorageAnalyzerUiState,
) {
    val clipBoardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
            onClick = {
                scope.launch {
                    val uiStateJson =
                        Json.encodeToString(StorageAnalyzerUiState.serializer(), uiState)
                    clipBoardManager.setClipEntry(
                        ClipEntry(
                            StringSelection(uiStateJson)
                        )
                    )
                }
            }) {
            Text("Copy")
        }
    }
}