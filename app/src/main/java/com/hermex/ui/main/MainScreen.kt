package com.hermex.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hermex.ui.chat.LiveChatScreen
import com.hermex.ui.theme.OptimusBackground
import com.hermex.ui.theme.OptimusSurface
import com.hermex.ui.theme.OptimusTextPrimary
import com.hermex.ui.theme.OptimusTextSecondary

/**
 * Temporary top-level shell during the feature migration.
 * All primary chat behavior is already gateway-backed; secondary product areas
 * are reintroduced one by one against the same domain/data boundaries.
 */
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit = {}
) {
    Scaffold(
        containerColor = OptimusBackground,
        topBar = {
            TopAppBar(
                title = { Text("Optimus", color = OptimusTextPrimary) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = OptimusTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OptimusSurface)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(OptimusBackground), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize()) {
                LiveChatScreen()
            }
        }
    }
}
