package cstjean.mobile2.classementlnh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cstjean.mobile2.classementlnh.ClassementLnhApplication
import cstjean.mobile2.classementlnh.ui.ClassementScreen
import cstjean.mobile2.classementlnh.viewmodel.ClassementViewModel

@Composable
fun LnhNavGraph() {
    val navController = rememberNavController()

    // Récupération du repository à partir du contexte de l'application
    val context = LocalContext.current
    val app = context.applicationContext as ClassementLnhApplication
    val repository = app.repository

    NavHost(
        navController = navController,
        startDestination = ClassementRoute
    ) {
        // 1. Route de l'écran principal
        composable<ClassementRoute> {
            ClassementScreen(
                viewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { ClassementViewModel(repository) }
                    })
            )
        }
    }
}