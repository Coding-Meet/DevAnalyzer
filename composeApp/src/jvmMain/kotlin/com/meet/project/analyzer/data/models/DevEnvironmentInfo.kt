package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DevEnvironmentInfo(
    val gradleCache: StorageInfo,
    val ideaCache: StorageInfo,
    val konanInfo: CacheInfo,
    val skikoInfo: CacheInfo,
    val konanInfos: List<KonanInfo>,
    val gradleInfos: List<GradleInfo>,
    val gradleWrapperInfos: List<GradleWrapperInfo>,
    val jdks: List<JdkInfo>
)
