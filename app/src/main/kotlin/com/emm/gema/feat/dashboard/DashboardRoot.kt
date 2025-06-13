package com.emm.gema.feat.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.gema.DashboardRoutes

@Composable
fun DashboardRoot(topNavController: NavController) {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoutes.Dashboard,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable<DashboardRoutes.Dashboard> {
                DashboardScreen()
            }

            composable<DashboardRoutes.Courses> {
                CoursesScreen()
            }

            composable<DashboardRoutes.Attendance> {
                AttendanceScreen()
            }

            composable<DashboardRoutes.Evaluations> {
                EvaluationsScreen()
            }

            composable<DashboardRoutes.Settings> {
                ProfileScreen()
            }
        }
    }
}