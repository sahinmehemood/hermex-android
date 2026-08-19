package com.hermex.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hermex.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignOut: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            SettingsSection(title = "Account") {
                ListItem(
                    headlineContent = { Text("Server URL") },
                    supportingContent = { Text("Connected server address") },
                    leadingContent = { Icon(Icons.Default.Link, null) }
                )
                ListItem(
                    headlineContent = { Text("Custom Headers") },
                    supportingContent = { Text("Proxy authentication headers") },
                    leadingContent = { Icon(Icons.Default.Http, null) }
                )
            }
        }

        item {
            SettingsSection(title = "Appearance") {
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text("System default") },
                    leadingContent = { Icon(Icons.Default.Palette, null) }
                )
                ListItem(
                    headlineContent = { Text("Font Size") },
                    leadingContent = { Icon(Icons.Default.FormatSize, null) }
                )
            }
        }

        item {
            SettingsSection(title = "Danger Zone") {
                ListItem(
                    headlineContent = { Text("Sign Out") },
                    supportingContent = { Text("Disconnect from this server") },
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable {
                        authViewModel.signOut()
                        onSignOut()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}
