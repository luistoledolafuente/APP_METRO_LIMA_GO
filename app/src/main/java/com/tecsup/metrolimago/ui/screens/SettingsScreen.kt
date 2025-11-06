package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Pantalla de Ajustes e Información (Req 5 y 6).
 * Este es el "Menú" que pediste.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel, // Lo pasamos para futuros ajustes (ej. idioma)
    onNavigateBack: () -> Unit
) {
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

            // --- Sección de Información (Req 5) ---
            item {
                Text("Información", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                InfoCard(
                    icon = Icons.Default.CreditCard,
                    title = "Tarifas y Métodos de Pago",
                    content = "La tarifa general para la Línea 1 y Línea 2 es de S/ 1.50..."
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.NewReleases,
                    title = "Avisos Importantes (Mock)",
                    content = "• (HOY) Servicio restringido en la Estación Central..."
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.Security,
                    title = "Consejos de Seguridad",
                    content = "• Cuida tus pertenencias en todo momento..."
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            // --- Sección de Ajustes (Req 6) ---
            item {
                Text("Ajustes de la App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                SettingRow(
                    icon = Icons.Default.Translate,
                    title = "Idioma",
                    subtitle = "Español (Latinoamérica)"
                )
            }
            // (Aquí iría un switch para Modo Oscuro, pero lo dejamos simple por ahora)
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

/**
 * Helper para las tarjetas de información (el que te di antes)
 */
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

/**
 * Helper para una fila de ajuste (ej. Idioma)
 */
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