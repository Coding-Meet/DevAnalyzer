package com.meet.dev.analyzer.core.utility

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object AppLinks {
    const val WEBSITE = "https://coding-meet.github.io/DevAnalyzer/"
    const val GITHUB_PROJECT =
        "https://www.github.com/Coding-Meet/DevAnalyzer"
    const val RELEASE_LINK =
        "https://github.com/Coding-Meet/DevAnalyzer/releases"
    const val REPORT_BUG =
        "https://www.github.com/Coding-Meet/DevAnalyzer/issues/new?template=bug_report.md"
    const val REQUEST_FEATURE =
        "https://www.github.com/Coding-Meet/DevAnalyzer/issues/new?template=feature_request.md"

    const val PORTFOLIO = "https://www.codingmeet.com"
    const val GITHUB = "https://www.github.com/Coding-Meet"
    const val LINKEDIN = "https://www.linkedin.com/in/coding-meet"
    const val INSTAGRAM = "https://www.instagram.com/codingmeet26/"
    const val YOUTUBE = "https://www.youtube.com/@codingmeet26?si=_2Mu6ozuCdYuqihA"
    const val EMAIL = "mailto:meetb2602@gmail.com"
    const val TWITTER = "https://www.x.com/CodingMeet"
    const val TELEGRAM = "https://www.telegram.me/Meetb26"
    const val HIRE_ME = "https://www.codingmeet.com/service"


    val socialLinks =
        listOf(
            SocialLink(Icons.Default.Code, "GitHub", GITHUB),
            SocialLink(Icons.Default.Work, "LinkedIn", LINKEDIN),
            SocialLink(Icons.Default.Photo, "Instagram", INSTAGRAM),
            SocialLink(Icons.Default.PlayArrow, "YouTube", YOUTUBE),
            SocialLink(Icons.Default.AlternateEmail, "Twitter/X", TWITTER),
            SocialLink(Icons.Default.Email, "Email", EMAIL),
            SocialLink(Icons.AutoMirrored.Filled.Send, "Telegram", TELEGRAM)
        )

}

data class SocialLink(
    val icon: ImageVector,
    val label: String,
    val url: String
)

