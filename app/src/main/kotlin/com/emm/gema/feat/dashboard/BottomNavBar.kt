package com.emm.gema.feat.dashboard

import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.emm.gema.DashboardRoutes
import com.emm.gema.hhRoutes

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = navBackStackEntry?.destination

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        hhRoutes.forEach { screen: DashboardRoutes ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute(screen::class) } == true,
                onClick = {
                    navController.navigate(screen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = LocalContentColor.current,
                        fontWeight = if (currentDestination?.hierarchy?.any { it.hasRoute(screen::class) } == true) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            )
        }
    }
}