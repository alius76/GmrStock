package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.data.getProcessRepository
import com.alius.gmrstock.domain.model.Process
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.ui.components.ProcessItem
import com.alius.gmrstock.ui.components.RatioProductionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProcessScreenContent(user: User, databaseUrl: String) {
    val repository = remember(databaseUrl) { getProcessRepository(databaseUrl) }
    var procesos by remember { mutableStateOf<List<Process>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(databaseUrl) {
        loading = true
        procesos = repository.listarProcesos()
        println("✅ Total de procesos obtenidos: ${procesos.size}")
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título y manejo de "sin procesos"
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Lotes en progreso",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                if (procesos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(160.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7F4))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = "Sin procesos",
                                    tint = Color(0xFF2E2E2E),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay procesos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E2E2E)
                                )
                            }
                        }
                    }
                }
            }

            // Lista horizontal de procesos
            item {
                if (procesos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(procesos) { proceso ->
                            ProcessItem(proceso = proceso)
                        }
                    }
                }
            }

            // Título de la gráfica
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Gráfico de producción del mes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Card de la gráfica
            item {
                RatioProductionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }
    }
}
