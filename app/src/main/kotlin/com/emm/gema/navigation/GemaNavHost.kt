package com.emm.gema.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.gema.feature.backup.BackupRoute
import com.emm.gema.home.HomeRoute

@Composable
fun GemaNavHost(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GemaRoutes.HOME,
        modifier = modifier,
    ) {
        composable(GemaRoutes.HOME) {
            HomeRoute(onNavigateToBackup = { navController.navigate(GemaRoutes.BACKUP) })
        }
        composable(GemaRoutes.BACKUP) {
            BackupRoute(onNavigateBack = { navController.popBackStack() })
        }
    }
}
