package com.hermex.ui.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hermex.data.model.DailyUsage
import com.hermex.data.model.ModelUsage

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val insights by viewModel.insights.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading && insights == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            insights?.let { data ->
                item {
                    OverviewCard(data)
                }
                item {
                    Text(
                        "Model Usage",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                items(data.modelUsage ?: emptyList()) { usage ->
                    ModelUsageCard(usage)
                }
                item {
                    Text(
                        "Daily Usage",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                items(data.dailyUsage ?: emptyList()) { usage ->
                    DailyUsageCard(usage)
                }
            }
        }
    }
}

@Composable
fun OverviewCard(data: com.hermex.data.model.InsightsResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Overview", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Messages", data.messageCount?.toString() ?: "-")
                StatItem("Sessions", data.sessionCount?.toString() ?: "-")
                StatItem("Input", "${data.inputTokens ?: 0}")
                StatItem("Output", "${data.outputTokens ?: 0}")
            }
            data.estimatedCost?.let {
                Text(
                    "Estimated cost: $$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ModelUsageCard(usage: ModelUsage) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(usage.model ?: "Unknown", style = MaterialTheme.typography.titleSmall)
            Text(usage.provider ?: "", style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${usage.count ?: 0} calls", style = MaterialTheme.typography.bodySmall)
                Text("${usage.inputTokens ?: 0} in / ${usage.outputTokens ?: 0} out", style = MaterialTheme.typography.bodySmall)
            }
            usage.estimatedCost?.let {
                Text("$$it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun DailyUsageCard(usage: DailyUsage) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(usage.date ?: "", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${usage.messageCount ?: 0} msgs", style = MaterialTheme.typography.bodySmall)
                Text("${usage.inputTokens ?: 0} in", style = MaterialTheme.typography.bodySmall)
                Text("${usage.outputTokens ?: 0} out", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
