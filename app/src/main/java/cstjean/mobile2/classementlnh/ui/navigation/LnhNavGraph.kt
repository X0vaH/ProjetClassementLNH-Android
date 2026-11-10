package cstjean.mobile2.classementlnh.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cstjean.mobile2.classementlnh.ClassementLnhApplication
import cstjean.mobile2.classementlnh.R
import cstjean.mobile2.classementlnh.auth.SessionManager
import cstjean.mobile2.classementlnh.ui.ClassementScreen
import cstjean.mobile2.classementlnh.ui.screen.LoginScreen
import cstjean.mobile2.classementlnh.ui.screen.ProfilScreen
import cstjean.mobile2.classementlnh.viewmodel.ClassementViewModel
import cstjean.mobile2.classementlnh.viewmodel.LoginViewModel
import cstjean.mobile2.classementlnh.viewmodel.ProfilViewModel

@Composable
fun LnhNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Récupération des dépendances à partir du contexte de l'application
    val context = LocalContext.current
    val app = context.applicationContext as ClassementLnhApplication
    val repository = app.repository

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<ClassementRoute>() == true,
                    onClick = {
                        navController.navigate(ClassementRoute) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.ic_classement), contentDescription = "Classement") },
                    label = { Text("Classement") }
                )
                // Nouveau Menu : Profil
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<ProfilRoute>() == true,
                    onClick = {
                        navController.navigate(ProfilRoute) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil") }
                )
            }
        }
    ) { innerPadding ->
        val session = SessionManager
        val jeton by session.jeton.collectAsStateWithLifecycle()

        // Si le jeton est null, on redirige immédiatement vers l'écran de connexion
        LaunchedEffect(jeton) {
            val surLaConnexion = navController.currentDestination?.hasRoute<LoginRoute>() == true
            if (jeton == null && !surLaConnexion) {
                navController.navigate(LoginRoute) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            // consumeWindowInsets évite que les Scaffold des écrans ajoutent une 2e fois les marges système
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable<LoginRoute> {
                LoginScreen(
                    viewModel = viewModel(factory = viewModelFactory {
                        initializer { LoginViewModel(app.authRepository) }
                    }),
                    onConnecte = {
                        navController.navigate(ClassementRoute) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    }
                )
            }

            composable<ProfilRoute> {
                ProfilScreen(
                    viewModel = viewModel(factory = viewModelFactory {
                        initializer { ProfilViewModel(app.authRepository, app.sessionManager) }
                    }),
                    onRetour = { navController.popBackStack() }
                )
            }

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
}
