package com.emm.gema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.emm.gema.data.network.auth.DataStore
import com.emm.gema.feat.auth.LoginScreen
import com.emm.gema.feat.auth.LoginViewModel
import com.emm.gema.feat.auth.RegisterScreen
import com.emm.gema.feat.auth.RegisterViewModel
import com.emm.gema.feat.dashboard.DashboardRoot
import com.emm.gema.feat.dashboard.course.CourseFormScreen
import com.emm.gema.feat.dashboard.course.CourseFormViewModel
import com.emm.gema.feat.dashboard.evaluation.EvaluationFormScreen
import com.emm.gema.feat.dashboard.student.StudentFormScreen
import com.emm.gema.feat.dashboard.student.StudentFormViewModel
import com.emm.gema.feat.dashboard.student.StudentListScreen
import com.emm.gema.feat.dashboard.student.StudentListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun Root(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GemaRoutes.Dashboard,
        modifier = modifier
    ) {

        composable<GemaRoutes.PreLogin> {
            val dataStore: DataStore = koinInject()

            LaunchedEffect(Unit) {
                val popUpToBuilder: PopUpToBuilder.() -> Unit = { inclusive = true }
                if (dataStore.token.isNotBlank()) {
                    navController.navigate(GemaRoutes.Dashboard) {
                        popUpTo(GemaRoutes.PreLogin, popUpToBuilder)
                    }
                } else {
                    navController.navigate(GemaRoutes.Login) {
                        popUpTo(GemaRoutes.PreLogin, popUpToBuilder)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }

        composable<GemaRoutes.Login> {
            val vm: LoginViewModel = koinViewModel()

            LaunchedEffect(vm.state.successLogin) {
                if (vm.state.successLogin) {
                    navController.navigate(GemaRoutes.Dashboard) {
                        popUpTo(GemaRoutes.Login) {
                            inclusive = true
                        }
                    }
                }
            }

            LoginScreen(
                state = vm.state,
                onAction = vm::onAction,
                navigateToRegister = { navController.navigate(GemaRoutes.Register) }
            )
        }

        composable<GemaRoutes.Register> {
            val vm: RegisterViewModel = koinViewModel()

            LaunchedEffect(vm.state.success) {
                if (vm.state.success) navController.popBackStack()
            }

            RegisterScreen(
                state = vm.state,
                onAction = vm::onAction,
                onBack = navController::popBackStack
            )
        }

        composable<GemaRoutes.Dashboard> {
            DashboardRoot(navController)
        }

        composable<GemaRoutes.CreateCourse> {
            val vm: CourseFormViewModel = koinViewModel()

            CourseFormScreen(
                state = vm.state,
                onAction = vm::onAction,
                onBack = { navController.navigateUp() },
            )
        }

        composable<GemaRoutes.StudentList> {
            val param = it.toRoute<GemaRoutes.StudentList>()

            val vm: StudentListViewModel = koinViewModel(
                parameters = { parametersOf(param.courseId) }
            )

            val students = vm.students.collectAsStateWithLifecycle()

            StudentListScreen(
                onAddStudent = { navController.navigate(GemaRoutes.CreateStudent(param.courseId)) },
                students = students.value,
                onBack = { navController.navigateUp() }
            )
        }

        composable<GemaRoutes.CreateStudent> {
            val param: GemaRoutes.CreateStudent = it.toRoute<GemaRoutes.CreateStudent>()

            val vm: StudentFormViewModel = koinViewModel(
                parameters = { parametersOf(param.courseId) }
            )

            LaunchedEffect(vm.state.isSuccessful) {
                if (vm.state.isSuccessful) {
                    navController.navigateUp()
                }
            }

            StudentFormScreen(
                state = vm.state,
                onAction = vm::dispatch,
                onBack = { navController.navigateUp() }
            )
        }

        composable<GemaRoutes.CreateEvaluation> {
            EvaluationFormScreen(
                courseId = it.arguments?.getString("courseId") ?: "",
                evaluationId = it.arguments?.getString("evaluationId")
            )
        }
    }
}