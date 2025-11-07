package com.tecsup.metrolimago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun InformacionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Tarifas y Métodos de Pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("• Tarifa general: S/ 1.50.\n• Tarifa estudiante: S/ 0.75.\n• Métodos: tarjeta recargable, efectivo, app móvil.")

        Spacer(Modifier.height(24.dp))

        Text("Avisos de Mantenimiento o Interrupciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("• [Mock] Estación Angamos cerrada por mantenimiento el 10/11 hasta 18:00.\n• [Mock] Servicio reducido en Línea 2 por obras el 12/11.\n• [Mock] Interrupción temporal por simulacro nacional, 15/11 12:00-14:00.")

        Spacer(Modifier.height(24.dp))

        Text("Consejos de Seguridad y Buenas Prácticas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            """
            • Mantente detrás de la línea amarilla en el andén.
            • Cuida tus pertenencias y mantén tu bolso cerrado.
            • No bloquees las puertas, permite subir y bajar con orden.
            • Ante cualquier emergencia, contacta al personal del metro.
            """.trimIndent()
        )
    }
}
