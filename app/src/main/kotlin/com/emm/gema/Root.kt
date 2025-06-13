package com.emm.gema

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.gema.data.auth.DataStore
import com.emm.gema.feat.auth.LoginScreen
import com.emm.gema.feat.auth.LoginViewModel
import com.emm.gema.feat.auth.RegisterScreen
import com.emm.gema.feat.auth.RegisterViewModel
import com.emm.gema.feat.dashboard.DashboardRoot
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
    }
}