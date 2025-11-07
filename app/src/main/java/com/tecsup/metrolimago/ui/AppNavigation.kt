package com.tecsup.metrolimago.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.metrolimago.ui.screens.HomeScreen
import com.tecsup.metrolimago.ui.screens.LineDetailScreen
import com.tecsup.metrolimago.ui.screens.RoutePlannerScreen
import com.tecsup.metrolimago.ui.screens.RouteResultScreen
import com.tecsup.metrolimago.ui.screens.StationDetailScreen
import com.tecsup.metrolimago.ui.screens.InformacionScreen // <--- AGREGA ESTA IMPORTACIÓN
import com.tecsup.metrolimago.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object RoutePlanner : Screen("route_planner")
    object RouteResult : Screen("route_result")
    object Informacion : Screen("informacion") // <--- AGREGA ESTA

    object LineDetail : Screen("line_detail/{lineId}") {
        fun createRoute(lineId: String) = "line_detail/$lineId"
    }

    object StationDetail : Screen("station_detail/{stationId}") {
        fun createRoute(stationId: String) = "station_detail/$stationId"
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // --- Pantalla Principal (Home) ---
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPlanner = {
                    viewModel.clearRouteSearch()
                    navController.navigate(Screen.RoutePlanner.route)
                },
                onLineClicked = { lineId ->
                    navController.navigate(Screen.LineDetail.createRoute(lineId))
                },
                navController = navController // <--- PASA NAVCONTROLLER!
            )
        }

        // --- Pantalla de Información Adicional ---
        composable(Screen.Informacion.route) {
            InformacionScreen()
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
