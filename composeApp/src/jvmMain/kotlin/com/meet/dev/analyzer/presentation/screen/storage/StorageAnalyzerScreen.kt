package com.meet.dev.analyzer.presentation.screen.storage

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
import androidx.navigation.NavBackStackEntry
import com.meet.dev.analyzer.core.utility.StorageAnalyzerTabs
import com.meet.dev.analyzer.presentation.components.ErrorLayout
import com.meet.dev.analyzer.presentation.components.ProgressStatusLayout
import com.meet.dev.analyzer.presentation.components.TabLayout
import com.meet.dev.analyzer.presentation.components.TabSlideAnimation
import com.meet.dev.analyzer.presentation.components.TopAppBar
import com.meet.dev.analyzer.presentation.screen.storage.components.AndroidSdkTabContent
import com.meet.dev.analyzer.presentation.screen.storage.components.AvdSystemImagesInfoTabContent
import com.meet.dev.analyzer.presentation.screen.storage.components.GradleTabContent
import com.meet.dev.analyzer.presentation.screen.storage.components.IdeInfoTabContent
import com.meet.dev.analyzer.presentation.screen.storage.components.KotlinNativeJdkTabContent
import com.meet.dev.analyzer.presentation.screen.storage.components.LibrariesTabContent
import com.meet.dev.analyzer.presentation.screen.storage.components.OverviewTabContent
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor
import java.awt.datatransfer.StringSelection

@Composable
fun StorageAnalyzerScreen(
    parentEntry: NavBackStackEntry,
) {
    val viewModel = koinViewModel<StorageAnalyzerViewModel>(
        viewModelStoreOwner = parentEntry
    )
    val uiState by viewModel.uiState.collectAsState()

//    DataClassTOJson(
//        uiState
//    )
    StorageAnalyzerContent(
        uiState = uiState,
        onEvent = viewModel::handleIntent,
    )
}

@Composable
fun StorageAnalyzerContent(
    uiState: StorageAnalyzerUiState,
    onEvent: (StorageAnalyzerIntent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "Development Storage Analyzer",
                icon = Icons.Default.Storage,
                actions = {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(
                            PointerIcon(
                                Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                                )
                            )
                        ), onClick = {
                            onEvent(StorageAnalyzerIntent.RefreshData)
                        }
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
            // Progress and status
            ProgressStatusLayout(
                isScanning = uiState.isScanning,
                scanProgress = uiState.scanProgress,
                scanStatus = uiState.scanStatus,
                scanElapsedTime = uiState.scanElapsedTime,
                modifier = Modifier.padding(10.dp)
            )

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
                        ErrorLayout(error = uiState.error, onClearError = {
                            onEvent(StorageAnalyzerIntent.ClearError)
                        })
                    }
                }
            }

            // Tab Row
            TabLayout(
                selectedTabIndex = uiState.selectedTabIndex,
                tabList = StorageAnalyzerTabs.entries,
                onClick = { previousTabIndex, currentTabIndex, storageAnalyzerTabs ->
                    onEvent(
                        StorageAnalyzerIntent.SelectTab(
                            previousTabIndex, currentTabIndex, storageAnalyzerTabs
                        )
                    )
                }
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

                    StorageAnalyzerTabs.IdeData -> {
                        IdeInfoTabContent(
                            ideDataInfo = uiState.storageAnalyzerInfo?.ideDataInfo,
                        )
                    }

                    StorageAnalyzerTabs.AvdAndSystemImages -> {
                        AvdSystemImagesInfoTabContent(
                            androidAvdInfo = uiState.storageAnalyzerInfo?.androidAvdInfo,
                            androidSdkInfo = uiState.storageAnalyzerInfo?.androidSdkInfo
                        )
                    }

                    StorageAnalyzerTabs.AndroidSdk -> {
                        AndroidSdkTabContent(
                            androidSdkInfo = uiState.storageAnalyzerInfo?.androidSdkInfo
                        )
                    }

                    StorageAnalyzerTabs.KotlinNativeJdk -> {
                        KotlinNativeJdkTabContent(
                            konanInfo = uiState.storageAnalyzerInfo?.konanInfo,
                            jdkInfo = uiState.storageAnalyzerInfo?.gradleInfo?.jdkInfo
                        )

                    }

                    StorageAnalyzerTabs.Gradle -> {
                        GradleTabContent(
                            gradleInfo = uiState.storageAnalyzerInfo?.gradleInfo,
                        )
                    }

                    StorageAnalyzerTabs.Libraries -> {
                        LibrariesTabContent(
                            gradleModulesInfo = uiState.storageAnalyzerInfo?.gradleInfo?.gradleModulesInfo
                        )
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