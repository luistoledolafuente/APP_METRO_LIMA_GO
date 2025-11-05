package com.tecsup.metrolimago.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// --- AÑADIR IMPORTS DE NUEVAS PANTALLAS ---
import com.tecsup.metrolimago.ui.screens.AllLinesScreen
import com.tecsup.metrolimago.ui.screens.AllStationsScreen
import com.tecsup.metrolimago.ui.screens.HomeScreen
import com.tecsup.metrolimago.ui.screens.LineDetailScreen
import com.tecsup.metrolimago.ui.screens.RoutePlannerScreen
import com.tecsup.metrolimago.ui.screens.RouteResultScreen
import com.tecsup.metrolimago.ui.screens.SplashScreen // (Import del Splash)
import com.tecsup.metrolimago.ui.screens.StationDetailScreen
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Define las rutas de navegación de forma segura (type-safe).
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash") // (Ruta del Splash)
    object Home : Screen("home")
    object RoutePlanner : Screen("route_planner")
    object RouteResult : Screen("route_result")

    // --- AÑADIR NUEVAS RUTAS ---
    object AllLines : Screen("all_lines")
    object AllStations : Screen("all_stations")

    object LineDetail : Screen("line_detail/{lineId}") {
        fun createRoute(lineId: String) = "line_detail/$lineId"
    }

    object StationDetail : Screen("station_detail/{stationId}") {
        fun createRoute(stationId: String) = "station_detail/$stationId"
    }
}

/**
 * El "NavHost" principal de la aplicación.
 * ACTUALIZADO para conectar Splash y "Ver todo".
 * (Esta es tu lógica original de navegación)
 */
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route // (Iniciamos en Splash)
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
                    viewModel.clearSelectedLine() // (Esta es tu lógica original)
                    navController.navigate(Screen.LineDetail.createRoute(lineId))
                },
                // --- CONECTAR LOS BOTONES "VER TODO" ---
                onStationClicked = { stationId ->
                    navController.navigate(Screen.StationDetail.createRoute(stationId))
                },
                onViewAllLines = {
                    navController.navigate(Screen.AllLines.route)
                },
                onViewAllStations = {
                    navController.navigate(Screen.AllStations.route)
                }
            )
        }

        // --- AÑADIR LOS COMPOSABLES DE LAS NUEVAS PANTALLAS ---

        // --- Pantalla "Todas las Líneas" ---
        composable(Screen.AllLines.route) {
            AllLinesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLineClicked = { lineId ->
                    // Va al detalle de la línea
                    viewModel.clearSelectedLine()
                    navController.navigate(Screen.LineDetail.createRoute(lineId))
                }
            )
        }

        // --- Pantalla "Todas las Estaciones" ---
        composable(Screen.AllStations.route) {
            AllStationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onStationClicked = { stationId ->
                    // Va al detalle de la estación
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
                        // ¡Esta es tu lógica original!
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
                    // ¡Esta es tu lógica original!
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