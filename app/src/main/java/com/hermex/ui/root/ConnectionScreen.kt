package com.hermex.ui.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionScreen(
    state: ConnectionUiState,
    onConnect: (url: String, token: String?, label: String) -> Unit
) {
    var url by remember(state.settings?.wsUrl) { mutableStateOf(state.settings?.wsUrl.orEmpty()) }
    var token by remember(state.settings?.token) { mutableStateOf(state.settings?.token.orEmpty()) }
    var label by remember(state.settings?.label) { mutableStateOf(state.settings?.label ?: "Hermes") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Cloud, contentDescription = null)
                    Column {
                        Text("Connect Optimus", style = MaterialTheme.typography.headlineSmall)
                        Text("Connect to your Hermes gateway", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Server name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("WebSocket URL") },
                    placeholder = { Text("wss://your-hermes-host/api/ws") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Gateway token (optional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) }
                )

                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = { onConnect(url.trim(), token.trim().ifBlank { null }, label.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.saving && url.trim().startsWith("ws")
                ) {
                    if (state.saving) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Connect")
                }
            }
        }
    }
}
