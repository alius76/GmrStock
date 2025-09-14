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
import com.alius.gmrstock.data.getRatioRepository
import com.alius.gmrstock.domain.model.Process
import com.alius.gmrstock.domain.model.Ratio
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.ui.components.ProcessItem
import com.alius.gmrstock.ui.components.RatioData
import com.alius.gmrstock.ui.components.RatioProductionCard
import com.alius.gmrstock.ui.components.generateRatioDataFromCollection
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProcessScreenContent(user: User, databaseUrl: String) {
    // Repositorios
    val processRepository = remember(databaseUrl) { getProcessRepository(databaseUrl) }
    val ratioRepository = remember(databaseUrl) { getRatioRepository(databaseUrl) }

    // Estados
    var procesos by remember { mutableStateOf<List<Process>>(emptyList()) }
    var ratios by remember { mutableStateOf<List<Ratio>>(emptyList()) }
    var ratioDataList by remember { mutableStateOf<List<RatioData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Cargar procesos y ratios
    LaunchedEffect(databaseUrl) {
        loading = true
        scope.launch {
            procesos = processRepository.listarProcesos()
            ratios = ratioRepository.listarRatiosDelMes()
            ratioDataList = generateRatioDataFromCollection(ratios)
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Bloque superior: título y subtítulo procesos ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = "Lotes en progreso",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Número de procesos: ${procesos.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // --- Lista de procesos ---
            item {
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
                } else {
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

            // --- Bloque producción del mes ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Producción del mes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Total ratios: ${ratioDataList.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // --- Gráfica ratios ---
            item {
                RatioProductionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    ratioDataList = ratioDataList
                )
            }
        }
    }
}
