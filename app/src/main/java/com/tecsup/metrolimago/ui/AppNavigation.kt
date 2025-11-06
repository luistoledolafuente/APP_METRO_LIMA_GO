package com.tecsup.metrolimago.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.metrolimago.ui.screens.AllLinesScreen
import com.tecsup.metrolimago.ui.screens.AllStationsScreen
import com.tecsup.metrolimago.ui.screens.HomeScreen
import com.tecsup.metrolimago.ui.screens.LineDetailScreen
import com.tecsup.metrolimago.ui.screens.RoutePlannerScreen
import com.tecsup.metrolimago.ui.screens.RouteResultScreen
// --- 1. AÑADIR IMPORTS ---
import com.tecsup.metrolimago.ui.screens.SettingsScreen
import com.tecsup.metrolimago.ui.screens.SplashScreen
import com.tecsup.metrolimago.ui.screens.StationDetailScreen
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Define las rutas de navegación de forma segura (type-safe).
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object RoutePlanner : Screen("route_planner")
    object RouteResult : Screen("route_result")
    object AllLines : Screen("all_lines")
    object AllStations : Screen("all_stations")

    // --- 2. AÑADIR NUEVA RUTA ---
    object Settings : Screen("settings")

    object LineDetail : Screen("line_detail/{lineId}") {
        fun createRoute(lineId: String) = "line_detail/$lineId"
    }

    object StationDetail : Screen("station_detail/{stationId}") {
        fun createRoute(stationId: String) = "station_detail/$stationId"
    }
}

/**
 * El "NavHost" principal de la aplicación.
 * ACTUALIZADO para conectar la pantalla de Ajustes.
 */
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

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
                onViewAllLines = {
                    navController.navigate(Screen.AllLines.route)
                },
                onViewAllStations = {
                    navController.navigate(Screen.AllStations.route)
                },
                // --- 3. CONECTAR EL BOTÓN DE AJUSTES ---
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // --- 4. AÑADIR EL COMPOSABLE DE LA NUEVA PANTALLA ---
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- (El resto de tus pantallas no cambian) ---

        composable(Screen.AllLines.route) {
            AllLinesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLineClicked = { lineId ->
                    viewModel.clearSelectedLine()
                    navController.navigate(Screen.LineDetail.createRoute(lineId))
                }
            )
        }

        composable(Screen.AllStations.route) {
            AllStationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onStationClicked = { stationId ->
                    navController.navigate(Screen.StationDetail.createRoute(stationId))
                }
            )
        }

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

        composable(Screen.RouteResult.route) {
            RouteResultScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.clearRouteSearch()
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

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