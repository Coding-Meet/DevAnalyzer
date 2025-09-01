package com.meet.project.analyzer.presentation.screen.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.meet.project.analyzer.core.utility.AppLogger
import org.koin.compose.viewmodel.koinViewModel

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

    StorageAnalyzerContent(
        uiState = uiState,
        onRefresh = { viewModel.handleIntent(StorageAnalyzerIntent.RefreshData) },
        onLoadAvds = { viewModel.handleIntent(StorageAnalyzerIntent.LoadAvds) },
        onLoadSdk = { viewModel.handleIntent(StorageAnalyzerIntent.LoadSdkInfo) },
        onLoadDevEnv = { viewModel.handleIntent(StorageAnalyzerIntent.LoadDevEnvironment) },
        onLoadGradleCaches = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleCaches) },
        onLoadGradleModules = { viewModel.handleIntent(StorageAnalyzerIntent.LoadGradleModules) }
    )
}
