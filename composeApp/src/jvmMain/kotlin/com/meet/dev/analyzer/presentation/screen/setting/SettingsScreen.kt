package com.meet.dev.analyzer.presentation.screen.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.meet.dev.analyzer.core.utility.AppLinks
import com.meet.dev.analyzer.core.utility.AppLinks.socialLinks
import com.meet.dev.analyzer.core.utility.getDefaultAndroidSdkPath
import com.meet.dev.analyzer.core.utility.getDefaultAvdLocationPath
import com.meet.dev.analyzer.core.utility.getDefaultGoogleFolderPaths
import com.meet.dev.analyzer.core.utility.getDefaultGradleHomePath
import com.meet.dev.analyzer.core.utility.getDefaultJdkFolderPaths
import com.meet.dev.analyzer.core.utility.getDefaultJetbrainsFolderPaths
import com.meet.dev.analyzer.core.utility.getDefaultKonanFolderPath
import com.meet.dev.analyzer.presentation.components.TopAppBar
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.setting.components.LinkSettingItem
import com.meet.dev.analyzer.presentation.screen.setting.components.PathPickerDialog
import com.meet.dev.analyzer.presentation.screen.setting.components.PathSettingItem
import com.meet.dev.analyzer.presentation.screen.setting.components.SettingsSection
import com.meet.dev.analyzer.presentation.screen.setting.components.SocialLinkButton
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor


@Composable
fun SettingsScreen(
    parentEntry: NavBackStackEntry,
) {
    val viewModel = koinViewModel<SettingsViewModel>(
        viewModelStoreOwner = parentEntry
    )
    val pathUiState by viewModel.pathSettingsState.collectAsStateWithLifecycle()
    val settingsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        pathUiState = pathUiState,
        settingsUiState = settingsUiState,
        onEvent = viewModel::onIntent,
    )
}

@Composable
fun SettingsScreenContent(
    pathUiState: PathUiState, settingsUiState: SettingsUiState, onEvent: (SettingsUiIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "Settings", icon = Icons.Default.Settings
            )
        }) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Scan Locations Section
                SettingsSection(title = "Scan Locations") {
                    PathSettingItem(
                        label = "Android SDK Path",
                        path = pathUiState.sdkPath,
                        status = pathUiState.sdkPathStatus,
                        icon = Icons.Default.Build,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(it, PathPickerType.ANDROID_SDK)
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateAndroidSdkPath(
                                    pathUiState.sdkPath
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateAndroidSdkPath(
                                    getDefaultAndroidSdkPath()
                                )
                            )
                        })

                    PathSettingItem(
                        label = "Gradle Home",
                        path = pathUiState.gradleHomePath,
                        status = pathUiState.gradleHomePathStatus,
                        icon = Icons.Default.Memory,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(it, PathPickerType.GRADLE_HOME)
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateGradleHomePath(
                                    pathUiState.gradleHomePath
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateGradleHomePath(
                                    getDefaultGradleHomePath()
                                )
                            )
                        })

                    PathSettingItem(
                        label = "AVD Location",
                        path = pathUiState.avdLocationPath,
                        status = pathUiState.avdLocationPathStatus,
                        icon = Icons.Default.PhoneAndroid,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(it, PathPickerType.AVD_LOCATION)
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateAvdLocationPath(
                                    pathUiState.avdLocationPath
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateAvdLocationPath(
                                    getDefaultAvdLocationPath()
                                )
                            )
                        })
//                    PathSettingItem(
//                        label = ".android Folder",
//                        path = pathUiState.androidFolderPath,
//                        status = pathUiState.androidFolderPathStatus,
//                        icon = Icons.Default.Folder,
//                        onEditClick = {
//                            onEvent(
//                                SettingsUiIntent.ShowPathPicker(it, PathPickerType.ANDROID_FOLDER)
//                            )
//                        },
//                        onValidateClick = {
//                            onEvent(
//                                SettingsUiIntent.UpdateAndroidFolderPath(
//                                    pathUiState.androidFolderPath
//                                )
//                            )
//                        },
//                        onResetDefaultClick = {
//                            onEvent(
//                                SettingsUiIntent.UpdateAndroidFolderPath(
//                                    getDefaultAndroidFolderPath()
//                                )
//                            )
//                        }
//                    )

                    PathSettingItem(
                        label = ".konan Folder",
                        path = pathUiState.konanFolderPath,
                        status = pathUiState.konanFolderPathStatus,
                        icon = Icons.Default.DataObject,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(it, PathPickerType.KONAN_FOLDER)
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateKonanFolderPath(
                                    pathUiState.konanFolderPath
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateKonanFolderPath(
                                    getDefaultKonanFolderPath()
                                )
                            )
                        })
                }

                HorizontalDivider()

                SettingsSection(title = "JDK Paths") {
                    PathSettingItem(
                        label = "JDK Path 1",
                        path = pathUiState.jdkPath1,
                        status = pathUiState.jdkPath1Status,
                        icon = Icons.Default.Code,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.JDK_1
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateJdkPath1(
                                    pathUiState.jdkPath1
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateJdkPath1(
                                    getDefaultJdkFolderPaths()[0]
                                )
                            )
                        })

                    PathSettingItem(
                        label = "JDK Path 2",
                        path = pathUiState.jdkPath2,
                        status = pathUiState.jdkPath2Status,
                        icon = Icons.Default.Code,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.JDK_2
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateJdkPath2(
                                    pathUiState.jdkPath2
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateJdkPath2(
                                    getDefaultJdkFolderPaths()[1]
                                )
                            )
                        })

                    PathSettingItem(
                        label = "JDK Path 3",
                        path = pathUiState.jdkPath3,
                        status = pathUiState.jdkPath3Status,
                        icon = Icons.Default.Code,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.JDK_3
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateJdkPath3(
                                    pathUiState.jdkPath3
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateJdkPath3(
                                    getDefaultJdkFolderPaths()[2]
                                )
                            )
                        })
                }

                HorizontalDivider()

                SettingsSection(title = "IDE - JetBrains") {
                    PathSettingItem(
                        label = "JetBrains Path 1",
                        path = pathUiState.ideJetBrains1,
                        status = pathUiState.ideJetBrains1Status,
                        icon = Icons.Default.Storage,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.IDE_JETBRAINS_1
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeJetBrains1(
                                    pathUiState.ideJetBrains1
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeJetBrains1(
                                    getDefaultJetbrainsFolderPaths()[0]
                                )
                            )
                        })

                    PathSettingItem(
                        label = "JetBrains Path 2",
                        path = pathUiState.ideJetBrains2,
                        status = pathUiState.ideJetBrains2Status,
                        icon = Icons.Default.Storage,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.IDE_JETBRAINS_2
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeJetBrains2(
                                    pathUiState.ideJetBrains2
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeJetBrains2(
                                    getDefaultJetbrainsFolderPaths()[1]
                                )
                            )
                        })

                    PathSettingItem(
                        label = "JetBrains Path 3",
                        path = pathUiState.ideJetBrains3,
                        status = pathUiState.ideJetBrains3Status,
                        icon = Icons.Default.Storage,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.IDE_JETBRAINS_3
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeJetBrains3(
                                    pathUiState.ideJetBrains3
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeJetBrains3(
                                    getDefaultJetbrainsFolderPaths()[2]
                                )
                            )
                        })
                }

                HorizontalDivider()

                SettingsSection(title = "IDE - Google") {
                    PathSettingItem(
                        label = "Google Path 1",
                        path = pathUiState.ideGoogle1,
                        status = pathUiState.ideGoogle1Status,
                        icon = Icons.Default.Storage,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.IDE_GOOGLE_1
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeGoogle1(
                                    pathUiState.ideGoogle1
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeGoogle1(
                                    getDefaultGoogleFolderPaths()[0]
                                )
                            )
                        })

                    PathSettingItem(
                        label = "Google Path 2",
                        path = pathUiState.ideGoogle2,
                        status = pathUiState.ideGoogle2Status,
                        icon = Icons.Default.Storage,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.IDE_GOOGLE_2
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeGoogle2(
                                    pathUiState.ideGoogle2
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeGoogle2(
                                    getDefaultGoogleFolderPaths()[1]
                                )
                            )
                        })

                    PathSettingItem(
                        label = "Google Path 3",
                        path = pathUiState.ideGoogle3,
                        status = pathUiState.ideGoogle3Status,
                        icon = Icons.Default.Storage,
                        onEditClick = {
                            onEvent(
                                SettingsUiIntent.ShowPathPicker(
                                    it, PathPickerType.IDE_GOOGLE_3
                                )
                            )
                        },
                        onValidateClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeGoogle3(
                                    pathUiState.ideGoogle3
                                )
                            )
                        },
                        onResetDefaultClick = {
                            onEvent(
                                SettingsUiIntent.UpdateIdeGoogle3(
                                    getDefaultGoogleFolderPaths()[2]
                                )
                            )
                        })
                }

                HorizontalDivider()


//                Data & Privacy Section
//                SettingsSection(title = "Data & Privacy") {
//                    SwitchSettingItem(
//                        label = "Crash Reporting",
//                        description = "Automatically report crashes to improve stability",
//                        checked = settingsUiState.crashReportingEnabled,
//                        icon = Icons.Default.BugReport,
//                        onCheckedChange = {
//                            onEvent(SettingsUiIntent.ToggleCrashReporting(it))
//                        })
//                }
//
//               HorizontalDivider()

                // About App Section
                SettingsSection(title = "About App") {
                    LinkSettingItem(
                        label = "Version",
                        value = AppLinks.VERSION,
                        icon = Icons.Default.Info,
                        url = AppLinks.RELEASE_LINK
                    )

                    LinkSettingItem(
                        label = "Website",
                        value = "https://coding-meet.github.io/DevAnalyzer/",
                        icon = Icons.Default.Language,
                        url = AppLinks.WEBSITE
                    )

                    LinkSettingItem(
                        label = "Project Repository",
                        value = "View on GitHub",
                        icon = Icons.Default.Code,
                        url = AppLinks.GITHUB_PROJECT
                    )
                }

                HorizontalDivider()

                // Feedback Section
                SettingsSection(title = "Feedback") {
                    LinkSettingItem(
                        label = "Report a Bug",
                        value = "Help us improve",
                        icon = Icons.Default.BugReport,
                        url = AppLinks.REPORT_BUG
                    )

                    LinkSettingItem(
                        label = "Request a Feature",
                        value = "Share your ideas",
                        icon = Icons.Default.Lightbulb,
                        url = AppLinks.REQUEST_FEATURE
                    )
                }

                HorizontalDivider()

                // Developer Section
                SettingsSection(title = "Developer") {
                    LinkSettingItem(
                        label = "Meet",
                        value = "www.codingmeet.com",
                        icon = Icons.Default.Person,
                        url = AppLinks.PORTFOLIO
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val uriHandler = LocalUriHandler.current

                            // Social Links Title
                            Text(
                                text = "Connect with me",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Social Links
                            Row(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                socialLinks.forEach { link ->
                                    SocialLinkButton(
                                        icon = link.icon,
                                        label = link.label,
                                        url = link.url
                                    )
                                }
                            }

                            HorizontalDivider()

                            // Hire Me Button
                            Button(
                                onClick = { uriHandler.openUri(AppLinks.HIRE_ME) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Hire Me",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
        }
    }

    settingsUiState.showPathPicker?.let { showPathPicker ->
        PathPickerDialog(
            type = showPathPicker,
            currentPath = settingsUiState.currentPath,
            onDismiss = {
                onEvent(SettingsUiIntent.ShowPathPicker("", null))
            },
            onPathSelected = { path ->
                when (showPathPicker) {
                    PathPickerType.ANDROID_SDK -> onEvent(SettingsUiIntent.UpdateAndroidSdkPath(path))

                    PathPickerType.GRADLE_HOME -> onEvent(SettingsUiIntent.UpdateGradleHomePath(path))

                    PathPickerType.AVD_LOCATION -> onEvent(
                        SettingsUiIntent.UpdateAvdLocationPath(
                            path
                        )
                    )

                    PathPickerType.ANDROID_FOLDER -> onEvent(
                        SettingsUiIntent.UpdateAndroidFolderPath(
                            path
                        )
                    )

                    PathPickerType.KONAN_FOLDER -> onEvent(
                        SettingsUiIntent.UpdateKonanFolderPath(
                            path
                        )
                    )

                    PathPickerType.JDK_1 -> onEvent(SettingsUiIntent.UpdateJdkPath1(path))
                    PathPickerType.JDK_2 -> onEvent(SettingsUiIntent.UpdateJdkPath2(path))
                    PathPickerType.JDK_3 -> onEvent(SettingsUiIntent.UpdateJdkPath3(path))

                    PathPickerType.IDE_JETBRAINS_1 -> onEvent(
                        SettingsUiIntent.UpdateIdeJetBrains1(
                            path
                        )
                    )

                    PathPickerType.IDE_JETBRAINS_2 -> onEvent(
                        SettingsUiIntent.UpdateIdeJetBrains2(
                            path
                        )
                    )

                    PathPickerType.IDE_JETBRAINS_3 -> onEvent(
                        SettingsUiIntent.UpdateIdeJetBrains3(
                            path
                        )
                    )

                    PathPickerType.IDE_GOOGLE_1 -> onEvent(SettingsUiIntent.UpdateIdeGoogle1(path))
                    PathPickerType.IDE_GOOGLE_2 -> onEvent(SettingsUiIntent.UpdateIdeGoogle2(path))
                    PathPickerType.IDE_GOOGLE_3 -> onEvent(SettingsUiIntent.UpdateIdeGoogle3(path))

                }
                onEvent(SettingsUiIntent.ShowPathPicker("", null))
            })
    }

}
