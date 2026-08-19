package com.hermex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.hermex.ui.root.OptimusRootScreen
import com.hermex.ui.theme.HermexTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HermexTheme(dynamicColor = false) {
                OptimusRootScreen()
            }
        }
    }
}
