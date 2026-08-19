package com.hermex.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hermex.ui.components.OptimusStatusPill
import com.hermex.ui.theme.OptimusBackground
import com.hermex.ui.theme.OptimusDivider
import com.hermex.ui.theme.OptimusSurface
import com.hermex.ui.theme.OptimusTextPrimary
import com.hermex.ui.theme.OptimusTextSecondary
import com.hermex.ui.theme.OptimusTextTertiary
import com.hermex.ui.sessions.SessionsScreen
import com.hermex.ui.tasks.TasksScreen
import com.hermex.ui.skills.SkillsScreen
import com.hermex.ui.workspace.WorkspaceScreen
import com.hermex.ui.memory.MemoryScreen
import com.hermex.ui.insights.InsightsScreen

sealed class Screen(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Sessions : Screen("sessions", "Chat", Icons.Outlined.ChatBubbleOutline)
    object Tasks : Screen("tasks", "Tasks", Icons.Outlined.Schedule)
    object Workspace : Screen("workspace", "Files", Icons.Outlined.FolderOpen)
    object Skills : Screen("skills", "Skills", Icons.Outlined.Build)
    object Memory : Screen("memory", "Memory", Icons.Outlined.Memory)
    object Insights : Screen("insights", "Insights", Icons.Outlined.BarChart)
}

@Composable
fun MainScreen(
    onSessionClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Sessions, Screen.Tasks, Screen.Workspace, Screen.Skills, Screen.Memory, Screen.Insights)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: Screen.Sessions.route

    BoxWithConstraints(Modifier.fillMaxSize().background(OptimusBackground)) {
        val wide = maxWidth >= 720.dp

        Scaffold(
            containerColor = OptimusBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Optimus", style = MaterialTheme.typography.titleLarge, color = OptimusTextPrimary)
                            Spacer(Modifier.width(10.dp))
                            OptimusStatusPill("Hermes", modifier = Modifier)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = OptimusTextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = OptimusSurface,
                        scrolledContainerColor = OptimusSurface
                    )
                )
            },
            bottomBar = {
                if (!wide) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = OptimusSurface,
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = { navigate(navController, screen.route) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(Modifier.fillMaxSize().padding(innerPadding)) {
                if (wide) {
                    NavigationRail(
                        containerColor = OptimusSurface,
                        header = {
                            Surface(
                                modifier = Modifier.padding(bottom = 12.dp).size(42.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = OptimusBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OptimusDivider)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("O", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    ) {
                        items.forEach { screen ->
                            NavigationRailItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = { navigate(navController, screen.route) }
                            )
                        }
                    }
                }

                Box(Modifier.weight(1f).fillMaxSize().background(OptimusBackground)) {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "optimus-route-transition"
                    ) { route ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Sessions.route,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(Screen.Sessions.route) { SessionsScreen(onSessionClick = onSessionClick) }
                            composable(Screen.Tasks.route) { TasksScreen() }
                            composable(Screen.Workspace.route) { WorkspaceScreen() }
                            composable(Screen.Skills.route) { SkillsScreen() }
                            composable(Screen.Memory.route) { MemoryScreen() }
                            composable(Screen.Insights.route) { InsightsScreen() }
                        }
                    }
                }
            }
        }
    }
}

private fun navigate(navController: androidx.navigation.NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
