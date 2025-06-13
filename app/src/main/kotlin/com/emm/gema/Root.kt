package com.emm.gema

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.gema.feat.auth.LoginScreen
import kotlinx.coroutines.delay

@Composable
fun Root(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GemaRoutes.PreLogin,
        modifier = modifier
    ) {

        composable<GemaRoutes.PreLogin> {

            LaunchedEffect(Unit) {
                delay(1000L)
                navController.navigate(GemaRoutes.Login)
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable<GemaRoutes.Login> {
            LoginScreen()
        }

        composable<GemaRoutes.Register> {
//            RegisterScreen()
        }
    }
}