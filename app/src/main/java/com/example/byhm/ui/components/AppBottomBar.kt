package com.example.byhm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.byhm.ui.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Dashboard, "Home", Icons.Filled.Home),
        BottomNavItem(Screen.Map, "Map", Icons.Filled.LocationOn),
        BottomNavItem(Screen.ResourceDirectory, "Resources", Icons.AutoMirrored.Filled.List),
        BottomNavItem(Screen.IncidentReport, "Report", Icons.Filled.ReportProblem),
        BottomNavItem(Screen.More, "More", Icons.Filled.MoreHoriz)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val isSelected = when (item.screen) {
                Screen.Dashboard -> currentRoute == Screen.Dashboard.route
                Screen.Map -> currentRoute == Screen.Map.route
                Screen.ResourceDirectory -> currentRoute == Screen.ResourceDirectory.route || currentRoute?.startsWith("resource_detail") == true
                Screen.IncidentReport -> currentRoute == Screen.IncidentReport.route
                Screen.More -> currentRoute == Screen.More.route ||
                        currentRoute == Screen.Alerts.route ||
                        currentRoute == Screen.AiAssistant.route ||
                        currentRoute == Screen.MyReports.route ||
                        currentRoute == Screen.Settings.route ||
                        currentRoute == Screen.EmergencyGuide.route ||
                        currentRoute == Screen.HelpInfo.route
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
