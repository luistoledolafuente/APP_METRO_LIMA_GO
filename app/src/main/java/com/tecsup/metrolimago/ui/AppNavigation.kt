package com.tecsup.metrolimago.ui // Asegúrate que coincida con tu paquete

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tecsup.metrolimago.ui.screens.AllLinesScreen
import com.tecsup.metrolimago.ui.screens.AllStationsScreen
import com.tecsup.metrolimago.ui.screens.FavoritesScreen
import com.tecsup.metrolimago.ui.screens.HomeScreen
import com.tecsup.metrolimago.ui.screens.LineDetailScreen
import com.tecsup.metrolimago.ui.screens.RoutePlannerScreen
import com.tecsup.metrolimago.ui.screens.RouteResultScreen
import com.tecsup.metrolimago.ui.screens.SettingsScreen
import com.tecsup.metrolimago.ui.screens.SplashScreen
import com.tecsup.metrolimago.ui.screens.StationDetailScreen
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Define las rutas de navegación de forma segura (type-safe).
 * (Incluye Splash, Favorites, Settings, etc.)
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object RoutePlanner : Screen("route_planner")
    object RouteResult : Screen("route_result")
    object AllLines : Screen("all_lines")
    object AllStations : Screen("all_stations")
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")

    object LineDetail : Screen("line_detail/{lineId}") {
        fun createRoute(lineId: String) = "line_detail/$lineId"
    }

    object StationDetail : Screen("station_detail/{stationId}") {
        fun createRoute(stationId: String) = "station_detail/$stationId"
    }
}

/**
 * El "NavHost" principal de la aplicación.
 * ¡ACTUALIZADO CON TODAS LAS CORRECCIONES!
 */
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // --- Pantalla Splash ---
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Pantalla Principal (Home) ---
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPlanner = {
                    viewModel.clearRouteSearch()
                    navController.navigate(Screen.RoutePlanner.route)
                },
                onLineClicked = { lineId ->
                    viewModel.clearSelectedLine()
                    navController.navigate(Screen.LineDetail.createRoute(lineId))
                },
                onStationClicked = { stationId ->
                    navController.navigate(Screen.StationDetail.createRoute(stationId))
                },
                onViewAllLines = { /* Redundante, manejado por BottomNav */ },
                onViewAllStations = { /* Redundante, manejado por BottomNav */ },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // --- Pantalla de Favoritos ---
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = viewModel,
                onStationClicked = { stationId ->
                    navController.navigate(Screen.StationDetail.createRoute(stationId))
                },
                onRouteClicked = {
                    navController.navigate(Screen.RouteResult.route)
                }
            )
        }

        // --- Pantalla de Ajustes (SIMPLIFICADA) ---
        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
                // 'onStationClicked' fue eliminado, causando el error. Ahora está corregido.
            )
        }
        // --- FIN DE LA CORRECCIÓN ---

        // --- Pantalla de Todas las Líneas ---
        composable(Screen.AllLines.route) {
            AllLinesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }, // <-- Corregido el typo
                onLineClicked = { lineId ->
                    viewModel.clearSelectedLine()
                    navController.navigate(Screen.LineDetail.createRoute(lineId))
                }
            )
        }

        // --- Pantalla de Todas las Estaciones ---
        composable(Screen.AllStations.route) {
            AllStationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onStationClicked = { stationId ->
                    navController.navigate(Screen.StationDetail.createRoute(stationId))
                }
            )
        }

        // --- Pantalla de Detalle de Línea ---
        composable(
            route = Screen.LineDetail.route,
            arguments = listOf(navArgument("lineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lineId = backStackEntry.arguments?.getString("lineId")
            if (lineId != null) {
                LineDetailScreen(
                    viewModel = viewModel,
                    lineId = lineId,
                    onNavigateBack = {
                        viewModel.clearSelectedLine()
                        navController.popBackStack()
                    },
                    onStationClicked = { stationId ->
                        navController.navigate(Screen.StationDetail.createRoute(stationId))
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        // --- Pantalla de Planificar Ruta ---
        composable(Screen.RoutePlanner.route) {
            RoutePlannerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.clearRouteSearch()
                    navController.popBackStack()
                },
                onNavigateToResult = {
                    navController.navigate(Screen.RouteResult.route)
                }
            )
        }

        // --- Pantalla de Resultado de Ruta ---
        composable(Screen.RouteResult.route) {
            RouteResultScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.clearRouteSearch()
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // --- Pantalla de Detalle de Estación ---
        composable(
            route = Screen.StationDetail.route,
            arguments = listOf(navArgument("stationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getString("stationId")
            if (stationId != null) {
                StationDetailScreen(
                    viewModel = viewModel,
                    stationId = stationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}