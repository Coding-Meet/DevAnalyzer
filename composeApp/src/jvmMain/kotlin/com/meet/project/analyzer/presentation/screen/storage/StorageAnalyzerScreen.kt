package com.meet.project.analyzer.presentation.screen.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import com.meet.project.analyzer.core.utility.AppLogger
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor
import java.awt.datatransfer.StringSelection

@Composable
fun StorageAnalyzerScreen() {
    val viewModel = koinViewModel<StorageAnalyzerViewModel>()
    val uiState by viewModel.uiState.collectAsState()


    // Handle error snackbar or dialog
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            AppLogger.e("StorageAnalyzerScreen") { "Error in UI: $error" }
            // Here you could show a snackbar or dialog
            // For now, we'll auto-clear after showing
            kotlinx.coroutines.delay(5000)
            viewModel.handleIntent(StorageAnalyzerIntent.ClearError)
        }
    }
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
        onLoadGradleModules = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleModules) }
    )
//    StorageAnalyzerContent1(
//        uiState = uiState,
//        onRefresh = { viewModel.handleIntent(StorageAnalyzerIntent.RefreshData) },
//        onLoadAvds = { viewModel.handleIntent(StorageAnalyzerIntent.LoadAvds) },
//        onLoadSdk = { viewModel.handleIntent(StorageAnalyzerIntent.LoadSdkInfo) },
//        onLoadDevEnv = { viewModel.handleIntent(StorageAnalyzerIntent.LoadDevEnvironment) },
//        onLoadGradleCaches = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleCaches) },
//        onLoadGradleModules = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleModules) }
//    )
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