package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getReprocesarRepository
import com.alius.gmrstock.domain.model.Reprocesar
import com.alius.gmrstock.ui.components.ReprocesarCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ReprocesarScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { getReprocesarRepository(databaseUrl) }

        var reprocesos by remember { mutableStateOf<List<Reprocesar>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var totalKilos by remember { mutableStateOf(0.0) }

        // 🔹 Cargar reprocesos
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    isLoading = true
                    val data = repository.listarReprocesos()
                    // 🔹 Ordenar por fecha de reproceso descendente
                    reprocesos = data.sortedByDescending { it.reprocesoDate }
                    totalKilos = reprocesos.sumOf { it.reprocesoLoteWeight.toDoubleOrNull() ?: 0.0 }
                } catch (e: Exception) {
                    reprocesos = emptyList()
                } finally {
                    isLoading = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (reprocesos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay reprocesos registrados.",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🔹 HEADER (flecha + título + subtítulo) ahora hace scroll
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                                }
                            }

                            Text(
                                text = "Reprocesos",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Text(
                                text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                        }
                    }

                    // 🔹 Lista de reprocesos
                    items(reprocesos) { reproceso ->
                        ReprocesarCard(reproceso)
                    }

                    // 🔹 Spacer final
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
