package com.meet.dev.analyzer.data.models.updater

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("body") val body: String
)
