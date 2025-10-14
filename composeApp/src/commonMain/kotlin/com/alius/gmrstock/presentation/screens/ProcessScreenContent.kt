package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.data.getProcessRepository
import com.alius.gmrstock.data.getRatioRepository
import com.alius.gmrstock.domain.model.Process
import com.alius.gmrstock.domain.model.Ratio
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.ui.components.*
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import com.alius.gmrstock.core.utils.formatWeight
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProcessScreenContent(user: User, databaseUrl: String) {
    val processRepository = remember(databaseUrl) { getProcessRepository(databaseUrl) }
    val ratioRepository = remember(databaseUrl) { getRatioRepository(databaseUrl) }

    // Estados
    var procesos by remember { mutableStateOf<List<Process>>(emptyList()) }
    var ratiosDelMes by remember { mutableStateOf<List<Ratio>>(emptyList()) }
    var ratiosDelAno by remember { mutableStateOf<List<Ratio>>(emptyList()) }
    var ratioDataList by remember { mutableStateOf<List<RatioData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isAnnual by remember { mutableStateOf(false) } // Mes/Año

    val scope = rememberCoroutineScope()

    // --- Función para actualizar lista de datos del gráfico ---
    fun updateRatioDataList() {
        ratioDataList = if (isAnnual) generateRatioDataByMonth(ratiosDelAno)
        else generateRatioDataFromCollection(ratiosDelMes)
    }

    // Total kilos derivado
    val totalKilos by derivedStateOf { ratioDataList.sumOf { it.totalWeight } }

    LaunchedEffect(databaseUrl) {
        loading = true
        scope.launch {
            procesos = processRepository.listarProcesos()
            ratiosDelMes = ratioRepository.listarRatiosDelMes()
            ratiosDelAno = ratioRepository.listarRatiosDelAno()
            updateRatioDataList()
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Título principal ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = "Lotes en progreso",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // --- Lista de procesos ---
            item {
                if (procesos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFF029083), Color(0xFF00BFA5))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.HourglassEmpty,
                                        contentDescription = "Sin procesos",
                                        tint = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay procesos activos",
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(procesos) { proceso -> ProcessItem(proceso = proceso) }
                    }
                }
            }

            // --- Bloque de producción ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔹 Solo el título
                    Text(
                        text = "Gráfico de producción",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 🔹 Fila con Total kilos a la izquierda y botones a la derecha
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )

                        MySegmentedButton(
                            options = listOf("Mes", "Año"),
                            selectedIndex = if (isAnnual) 1 else 0,
                            onSelect = {
                                isAnnual = it == 1
                                updateRatioDataList()
                            }
                        )
                    }
                }
            }

            // --- Gráfico ---
            item {
                // 🔹 Usamos safe copy dentro del Card
                RatioProductionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    ratioDataList = ratioDataList,
                    isAnnual = isAnnual
                )
            }
        }
    }
}
