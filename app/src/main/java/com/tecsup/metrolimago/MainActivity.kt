package com.tecsup.metrolimago

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Para que la app ocupe toda la pantalla
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.metrolimago.ui.AppNavigation
import com.tecsup.metrolimago.ui.theme.MetroLimaGoTheme
import com.tecsup.metrolimago.viewmodel.MainViewModel
import com.tecsup.metrolimago.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita que la app se dibuje "detrás" de las barras de sistema (edge-to-edge)
        enableEdgeToEdge()

        setContent {
            // 1. Aplicamos nuestro tema personalizado (Modo Claro/Oscuro)
            MetroLimaGoTheme {

                // 2. Creamos la instancia principal del ViewModel
                // Usamos la Factory para poder pasarle el "Application" (Context)
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(
                        LocalContext.current.applicationContext as Application
                    )
                )

                // 3. Llamamos a nuestro Navegador
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // AppNavigation ahora controla toda la app
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}