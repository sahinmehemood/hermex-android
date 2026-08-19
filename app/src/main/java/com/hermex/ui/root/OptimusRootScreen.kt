package com.hermex.ui.root

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.hermex.ui.main.MainScreen

@Composable
fun OptimusRootScreen(
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val state = connectionViewModel.state.value

    when {
        state.loading -> ConnectionLoadingScreen()
        state.settings?.wsUrl.isNullOrBlank() -> ConnectionScreen(
            state = state,
            onConnect = connectionViewModel::save
        )
        else -> MainScreen(
            onNavigateToSettings = { connectionViewModel.clear() }
        )
    }
}

@Composable
private fun ConnectionLoadingScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}
