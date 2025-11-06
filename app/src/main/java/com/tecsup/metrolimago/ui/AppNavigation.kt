package com.tecsup.metrolimago.ui // Asegúrate que coincida con tu paquete

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController // <-- 1. IMPORTAR ESTO
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
// import androidx.navigation.compose.rememberNavController // <-- 2. YA NO SE USA AQUÍ
import androidx.navigation.navArgument
import com.tecsup.metrolimago.ui.screens.AllLinesScreen
import com.tecsup.metrolimago.ui.screens.AllStationsScreen
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
 * (El código de tu compañero se queda igual)
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object RoutePlanner : Screen("route_planner")
    object RouteResult : Screen("route_result")
    object AllLines : Screen("all_lines")
    object AllStations : Screen("all_stations")
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
 * AHORA RECIBE el NavController desde MainActivity.
 */
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController // <-- 3. ACEPTAR NavController COMO PARÁMETRO
) {
    // val navController = rememberNavController() // <-- 4. ELIMINAR ESTA LÍNEA

    NavHost(
        navController = navController, // <-- 5. Usar el parámetro
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
                // --- 6. SIMPLIFICADO: Estos ya no los necesita el HomeScreen ---
                onViewAllLines = {
                    // Esta lógica ahora vive en la Bottom Nav Bar
                },
                onViewAllStations = {
                    // Esta lógica ahora vive en la Bottom Nav Bar
                },
                onNavigateToSettings = {
                    // Esta lógica la podemos pasar al HomeSearchBar
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