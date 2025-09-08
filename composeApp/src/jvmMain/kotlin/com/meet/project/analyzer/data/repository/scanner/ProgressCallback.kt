package com.meet.project.analyzer.data.repository.scanner

interface ProgressCallback {
    suspend fun updateProgress(progress: Float, status: String)
}