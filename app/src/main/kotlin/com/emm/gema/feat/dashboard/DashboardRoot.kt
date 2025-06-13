package com.emm.gema.feat.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.gema.DashboardRoutes
import com.emm.gema.GemaRoutes
import com.emm.gema.data.auth.DataStore
import org.koin.compose.koinInject

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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Evaluations")
                }
            }

            composable<DashboardRoutes.Settings> {
                val dataStore: DataStore = koinInject()

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            dataStore.storeToken("")
                            topNavController.navigate(GemaRoutes.Login) {
                                popUpTo(GemaRoutes.Dashboard) {
                                    inclusive = true
                                }
                            }
                        }
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}