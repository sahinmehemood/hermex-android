package com.hermex.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.hermex.ui.sessions.SessionsScreen
import com.hermex.ui.tasks.TasksScreen
import com.hermex.ui.skills.SkillsScreen
import com.hermex.ui.workspace.WorkspaceScreen
import com.hermex.ui.memory.MemoryScreen
import com.hermex.ui.insights.InsightsScreen

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Sessions : Screen("sessions", "Chat", Icons.Default.ChatBubbleOutline)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Schedule)
    object Workspace : Screen("workspace", "Files", Icons.Default.FolderOpen)
    object Skills : Screen("skills", "Skills", Icons.Default.Build)
    object Memory : Screen("memory", "Memory", Icons.Default.Memory)
    object Insights : Screen("insights", "Insights", Icons.Default.BarChart)
}

@Composable
fun MainScreen(
    onSessionClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Sessions, Screen.Tasks, Screen.Workspace, Screen.Skills, Screen.Memory, Screen.Insights)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hermex") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Sessions.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Sessions.route) {
                SessionsScreen(onSessionClick = onSessionClick)
            }
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            composable(Screen.Workspace.route) {
                WorkspaceScreen()
            }
            composable(Screen.Skills.route) {
                SkillsScreen()
            }
            composable(Screen.Memory.route) {
                MemoryScreen()
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
        }
    }
}
