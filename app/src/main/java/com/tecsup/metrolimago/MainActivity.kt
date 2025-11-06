package com.tecsup.metrolimago // Asegúrate que coincida con tu paquete

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tecsup.metrolimago.ui.Screen // Importa el Screen de tu AppNavigation
import com.tecsup.metrolimago.ui.theme.MetroLimaGoTheme
import com.tecsup.metrolimago.viewmodel.MainViewModel
import com.tecsup.metrolimago.viewmodel.MainViewModelFactory

/**
 * Definimos los ítems de nuestro menú inferior.
 * Conectan la UI con las rutas de AppNavigation.
 */
sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home)
    object Lines : BottomNavItem(Screen.AllLines.route, "Líneas", Icons.Default.List)
    object Stations : BottomNavItem(Screen.AllStations.route, "Estaciones", Icons.Default.LocationOn)
    object Settings : BottomNavItem(Screen.Settings.route, "Ajustes", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MetroLimaGoTheme {

                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(
                        LocalContext.current.applicationContext as Application
                    )
                )

                // 1. Creamos el NavController aquí, en el nivel más alto.
                val navController = rememberNavController()

                // 2. Lista de pantallas principales para la Bottom Bar
                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Lines,
                    BottomNavItem.Stations,
                    BottomNavItem.Settings
                )

                // 3. Lógica para saber en qué pantalla estamos
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 4. Lógica para MOSTRAR u OCULTAR la Bottom Bar
                //    (Solo la mostramos en las 4 pantallas principales)
                //    También ocultamos en Splash
                val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

                Scaffold(
                    // 5. Asignamos la Bottom Bar
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
                    // 6. Llamamos al NavHost (AppNavigation) DENTRO del Scaffold
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding), // Aplicamos el padding del Scaffold
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            viewModel = viewModel,
                            navController = navController // Pasamos el NavController
                        )
                    }
                }
            }
        }
    }
}