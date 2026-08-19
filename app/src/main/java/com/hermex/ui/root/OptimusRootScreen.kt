package com.hermex.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hermex.ui.main.MainScreen

@Composable
fun OptimusRootScreen(
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val state by connectionViewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> ConnectionLoadingScreen()
        state.settings?.wsUrl.isNullOrBlank() -> ConnectionScreen(
            state = state,
            onConnect = connectionViewModel::save
        )
        else -> MainScreen(
            onNavigateToSettings = connectionViewModel::clear
        )
    }
}

@Composable
private fun ConnectionLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
