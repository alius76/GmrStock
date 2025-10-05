package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.core.utils.formatWeight // ⬅️ ¡Importación necesaria!
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.VentaChartCard
import com.alius.gmrstock.ui.components.VentaData
import com.alius.gmrstock.ui.components.VentaItem
import com.alius.gmrstock.ui.components.VentaItemSmall
import com.alius.gmrstock.ui.components.generateVentaDataFromCollection
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransferScreenContent(user: User, databaseUrl: String) {
    val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }

    // Estados
    var ventasHoy by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ultimasVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ventasDelMes by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ventaDataList by remember { mutableStateOf<List<VentaData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // 🆕 Estado derivado para calcular el total de kilos de ventas del mes.
    val totalKilosVentaMes by remember {
        derivedStateOf {
            // Se calcula el peso total como Double para evitar pérdida de precisión y usar formatWeight
            ventaDataList.sumOf {
                try {
                    // ASUME que VentaData tiene una propiedad de peso (ej: totalWeight) que se puede convertir a Double.
                    // Ajusta 'it.totalWeight' si tu VentaData usa otro nombre para el campo de peso sumable.
                    it.totalWeight.toDouble()
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    val hoyListState = rememberLazyListState()

    LaunchedEffect(databaseUrl) {
        loading = true
        scope.launch {
            ventasHoy = ventaRepository.mostrarLasVentasDeHoy()
            ultimasVentas = ventaRepository.mostrarLasUltimasVentas()
            ventasDelMes = ventaRepository.mostrarVentasDelMes()
            // NOTA: generateVentaDataFromCollection debe asegurarse de que VentaData contenga el peso.
            ventaDataList = generateVentaDataFromCollection(ventasDelMes)
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = com.alius.gmrstock.ui.theme.PrimaryColor
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... (Ventas de Hoy) ...
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = "Ventas de hoy",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (ventasHoy.isEmpty()) {
                    // ... (Card de Sin Ventas)
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoneyOff,
                                        contentDescription = "Sin ventas",
                                        tint = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sin ventas hoy",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyRow(
                        state = hoyListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        // ... (FlingBehavior)
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ventasHoy) { venta ->
                            VentaItemSmall(venta)
                        }
                    }
                }
            }

            // --- Ventas del Mes (MODIFICADO) ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Título principal
                    Text(
                        text = "Ventas del mes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // ⬅️ ¡CORRECCIÓN! Formato de peso aplicado aquí
                    Text(
                        text = "Total kilos: ${formatWeight(totalKilosVentaMes)} Kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                VentaChartCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    ventaDataList = ventaDataList
                )
            }

            // ... (Últimas Ventas) ...
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Últimas ventas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(ultimasVentas) { venta ->
                VentaItem(venta)
            }
        }
    }
}