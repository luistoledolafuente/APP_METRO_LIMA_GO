package com.tecsup.metrolimago // Asegúrate que coincida con tu paquete

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge // Eliminado para evitar conflictos de manifest
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
// --- 1. IMPORTAR ÍCONOS CORREGIDOS Y NUEVOS ---
import androidx.compose.material.icons.automirrored.filled.List // Corregido
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star // <-- ¡NUEVO ÍCONO!
// --- FIN DE IMPORTS ---
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tecsup.metrolimago.ui.AppNavigation
import com.tecsup.metrolimago.ui.Screen
import com.tecsup.metrolimago.ui.theme.MetroLimaGoTheme
import com.tecsup.metrolimago.viewmodel.MainViewModel
import com.tecsup.metrolimago.viewmodel.MainViewModelFactory
import com.tecsup.metrolimago.viewmodel.ThemeSetting

/**
 * Definimos los ítems de nuestro menú inferior.
 * ¡ACTUALIZADO CON FAVORITOS!
 */
sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home)
    // --- 2. ¡NUEVO ÍTEM AÑADIDO! ---
    object Favorites : BottomNavItem(Screen.Favorites.route, "Favoritos", Icons.Default.Star)
    object Lines : BottomNavItem(Screen.AllLines.route, "Líneas", Icons.AutoMirrored.Filled.List)
    object Stations : BottomNavItem(Screen.AllStations.route, "Estaciones", Icons.Default.LocationOn)
    object Settings : BottomNavItem(Screen.Settings.route, "Ajustes", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge() // Quitamos esto

        setContent {

            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    LocalContext.current.applicationContext as Application
                )
            )

            val uiState by viewModel.uiState.collectAsState()
            val useDarkTheme = when (uiState.theme) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            MetroLimaGoTheme(darkTheme = useDarkTheme) {

                val navController = rememberNavController()

                // --- 3. ¡LISTA ACTUALIZADA CON 5 ÍTEMS! ---
                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Favorites, // <-- Añadido
                    BottomNavItem.Lines,
                    BottomNavItem.Stations,
                    BottomNavItem.Settings
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}