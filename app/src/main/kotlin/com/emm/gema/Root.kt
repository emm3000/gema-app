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
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.emm.gema.data.auth.DataStore
import com.emm.gema.feat.auth.LoginScreen
import com.emm.gema.feat.auth.LoginViewModel
import com.emm.gema.feat.auth.RegisterScreen
import com.emm.gema.feat.auth.RegisterViewModel
import com.emm.gema.feat.dashboard.DashboardRoot
import com.emm.gema.feat.dashboard.forms.StudentListScreen
import com.emm.gema.feat.dashboard.forms.courseform.CourseFormScreen
import com.emm.gema.feat.dashboard.forms.courseform.CourseFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun Root(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GemaRoutes.PreLogin,
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
            StudentListScreen(
                courseId = param.courseId,
                onBack = { navController.navigateUp() }
            )
        }
    }
}