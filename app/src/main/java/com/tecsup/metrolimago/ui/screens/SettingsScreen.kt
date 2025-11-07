package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// --- 1. IMPORT DE TARJETA ELIMINADO (Ya no se usa) ---
// import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsSystemDaydream
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // <-- Importar
import androidx.compose.runtime.getValue // <-- Importar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.metrolimago.viewmodel.MainViewModel
import com.tecsup.metrolimago.viewmodel.ThemeSetting // <-- Importar el Enum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
) {
    // --- 2. OBTENER EL ESTADO ACTUAL DE LA UI ---
    val uiState by viewModel.uiState.collectAsState()

    // --- 3. ¡TEXTOS EXTENDIDOS! ---
    val travelTips = """
        • (AVISO) Servicio restringido en Estación Central (Simulación).
        • Planifica tu ruta antes de salir de casa.
        • Recarga tu tarjeta con anticipación para evitar colas.
        • Cede el asiento a quien lo necesite (adultos mayores, embarazadas, etc.).
    """.trimIndent()

    val securityTips = """
        • Mantén tus pertenencias (móvil, billetera) en un lugar seguro y a la vista, preferiblemente en bolsillos delanteros.
        • Evita llevar objetos de valor a la vista.
        • En horas punta, sujeta bien tu mochila o cartera por delante.
        • No aceptes ayuda de extraños en los cajeros o máquinas de recarga.
        • Respeta la línea amarilla y espera el tren detrás de ella.
    """.trimIndent()


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes e Información") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text("Información", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // --- 4. ¡TARJETA DE TARIFAS ELIMINADA! ---
            // Se ha quitado el item que contenía la InfoCard de CreditCard

            item {
                InfoCard(
                    icon = Icons.Default.NewReleases,
                    title = "Avisos y Recomendaciones", // Título actualizado
                    content = travelTips // Contenido actualizado
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.Security,
                    title = "Consejos de Seguridad",
                    content = securityTips // Contenido actualizado
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            item {
                Text("Ajustes de la App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Text("Apariencia", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                ThemeSelector(
                    currentTheme = uiState.theme,
                    onThemeSelected = { newTheme ->
                        viewModel.setTheme(newTheme) // Llama al ViewModel
                    }
                )
            }

            item {
                SettingRow(
                    icon = Icons.Default.Info,
                    title = "Versión",
                    subtitle = "1.0.0 (Beta)"
                )
            }
        }
    }
}

// (InfoCard se queda igual)
@Composable
fun InfoCard(icon: ImageVector, title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// (SettingRow se queda igual)
@Composable
fun SettingRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// (ThemeSelector y sus funciones auxiliares se quedan igual)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    currentTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    val options = listOf(ThemeSetting.LIGHT, ThemeSetting.DARK, ThemeSetting.SYSTEM)

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { theme ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = options.indexOf(theme), count = options.size),
                onClick = { onThemeSelected(theme) },
                selected = currentTheme == theme
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = getIconForTheme(theme),
                        contentDescription = getLabelForTheme(theme),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(getLabelForTheme(theme))
                }
            }
        }
    }
}

private fun getLabelForTheme(theme: ThemeSetting): String {
    return when (theme) {
        ThemeSetting.LIGHT -> "Claro"
        ThemeSetting.DARK -> "Oscuro"
        ThemeSetting.SYSTEM -> "Sistema"
    }
}

private fun getIconForTheme(theme: ThemeSetting): ImageVector {
    return when (theme) {
        ThemeSetting.LIGHT -> Icons.Outlined.LightMode
        ThemeSetting.DARK -> Icons.Outlined.DarkMode
        ThemeSetting.SYSTEM -> Icons.Outlined.SettingsSystemDaydream
    }
}