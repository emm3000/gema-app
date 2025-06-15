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
import com.emm.gema.GemaRoutes
import com.emm.gema.feat.dashboard.attendance.AttendanceScreen
import com.emm.gema.feat.dashboard.attendance.AttendanceViewModel
import com.emm.gema.feat.dashboard.course.CourseViewModel
import com.emm.gema.feat.dashboard.course.CoursesScreen
import org.koin.androidx.compose.koinViewModel

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
                val vm: CourseViewModel = koinViewModel()

                CoursesScreen(
                    state = vm.state,
                    retryFetchCourses = { vm.fetchCourses() },
                    createCourse = { topNavController.navigate(GemaRoutes.CreateCourse) },
                    toStudentList = { courseId -> topNavController.navigate(GemaRoutes.StudentList(courseId)) }
                )
            }

            composable<DashboardRoutes.Attendance> {
                val vm: AttendanceViewModel = koinViewModel()

                AttendanceScreen(
                    state = vm.state,
                    onAction = vm::onAction,
                )
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