package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.VentaChartCard
import com.alius.gmrstock.ui.components.VentaData
import com.alius.gmrstock.ui.components.VentaItem
import com.alius.gmrstock.ui.components.VentaItemSmall
import com.alius.gmrstock.ui.components.generateVentaDataFromCollection
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

    val scope = rememberCoroutineScope()
    val hoyListState = rememberLazyListState()

    LaunchedEffect(databaseUrl) {
        loading = true
        scope.launch {
            ventasHoy = ventaRepository.mostrarLasVentasDeHoy()
            ultimasVentas = ventaRepository.mostrarLasUltimasVentas()
            ventasDelMes = ventaRepository.mostrarVentasDelMes()
            ventaDataList = generateVentaDataFromCollection(ventasDelMes)
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
            // --- Ventas de Hoy ---
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Número de ventas: ${ventasHoy.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (ventasHoy.isEmpty()) {
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
                                    contentDescription = "Sin ventas",
                                    tint = Color(0xFF2E2E2E),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sin ventas hoy",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E2E2E)
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        state = hoyListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = hoyListState),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ventasHoy) { venta ->
                            VentaItemSmall(venta)
                        }
                    }
                }
            }

            // --- Ventas del Mes ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ventas del mes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )


                    Text(
                        text = "Días con ventas: ${ventaDataList.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
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

            // --- Últimas Ventas ---
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Total: ${ultimasVentas.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
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
