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
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.data.getDevolucionRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.domain.model.Devolucion
import com.alius.gmrstock.ui.components.*
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransferScreenContent(user: User, databaseUrl: String) {
    val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }
    val devolucionRepository = remember(databaseUrl) { getDevolucionRepository(databaseUrl) }

    // Estados de Ventas
    var ventasHoy by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ultimasVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ventasDelMes by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ventasDelAnio by remember { mutableStateOf<List<Venta>>(emptyList()) }

    // Estados de Devoluciones
    var devolucionesDelMes by remember { mutableStateOf<List<Devolucion>>(emptyList()) }
    var devolucionesDelAnio by remember { mutableStateOf<List<Devolucion>>(emptyList()) } // Todas las devoluciones del año

    // Estados de UI y control
    var ventaDataList by remember { mutableStateOf<List<VentaData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isAnnual by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val hoyListState = rememberLazyListState()

    // Helper para obtener peso total de una lista de devoluciones
    fun calculateTotalDevoluciones(devoluciones: List<Devolucion>): Double {
        var total = 0.0
        println("\n--- INICIO CÁLCULO DEVOLUCIONES ---")
        devoluciones.forEachIndexed { index, devolucion ->
            val pesoString = devolucion.devolucionPesoTotal
            val pesoDoble = pesoString?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            total += pesoDoble
            println("DEVOLUCIÓN #${index + 1}: Fecha=${devolucion.devolucionFecha}, Peso String='$pesoString', Peso Double=$pesoDoble")
        }
        println("TOTAL CALCULADO: $total")
        println("--- FIN CÁLCULO DEVOLUCIONES ---\n")
        return total
    }

    // --- Métrica Derivada para el Peso Devuelto ACTUAL ---
    val totalKilosDevueltosActual by derivedStateOf {
        val listaUsada = if (isAnnual) "DEVOLUCIONES DEL AÑO" else "DEVOLUCIONES DEL MES"
        val kilos = if (isAnnual) {
            calculateTotalDevoluciones(devolucionesDelAnio)
        } else {
            calculateTotalDevoluciones(devolucionesDelMes)
        }
        println("RENDERIZANDO: Selector en ${if (isAnnual) "AÑO" else "MES"}. Usando lista: $listaUsada. Kilos mostrados: $kilos Kg")
        kilos
    }

    // --- Actualización de los datos del gráfico ---
    fun updateVentaDataList() {
        ventaDataList = if (isAnnual) {
            generateVentaDataByMonth(ventasDelAnio)
        } else {
            generateVentaDataFromCollection(ventasDelMes)
        }
    }

    // Total kilos derivados (de ventas)
    val totalKilosVentas by derivedStateOf { ventaDataList.sumOf { it.totalWeight.toDouble() } }

    LaunchedEffect(databaseUrl) {
        loading = true
        scope.launch {
            // Cargar datos de Ventas
            ventasHoy = ventaRepository.mostrarLasVentasDeHoy()
            ultimasVentas = ventaRepository.mostrarLasUltimasVentas()
            ventasDelMes = ventaRepository.mostrarVentasDelMes()
            ventasDelAnio = ventaRepository.mostrarVentasPorCliente("") // todas del año

            // 3. CARGAR DATOS DE DEVOLUCIONES
            devolucionesDelMes = devolucionRepository.obtenerDevolucionesDelMes()
            println("DEBUG LOAD: devolucionesDelMes cargadas: ${devolucionesDelMes.size} elementos.")

            devolucionesDelAnio = devolucionRepository.obtenerTodasLasDevoluciones()
            println("DEBUG LOAD: devolucionesDelAnio (Todas las devoluciones) cargadas: ${devolucionesDelAnio.size} elementos.")

            // Inspección del contenido cargado
            devolucionesDelAnio.firstOrNull()?.let {
                println("DEBUG INSPECCIÓN: Primer Devolución del Año -> Fecha: ${it.devolucionFecha}, Peso: ${it.devolucionPesoTotal}")
            }

            updateVentaDataList()
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (ventasHoy.isEmpty()) {
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ventasHoy) { venta -> VentaItemSmall(venta) }
                    }
                }
            }

            // --- Gráfico de Ventas ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔹 Título
                    Text(
                        text = "Gráfico de ventas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🔹 Fila de Métricas y Selector (Mes/Año)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Métrica de Ventas y Devoluciones agrupadas
                        Column {
                            // 🔹 Subtítulo de Ventas
                            Text(
                                text = "Total kilos: ${formatWeight(totalKilosVentas)} Kg",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextSecondary
                            )
                            // 🔹 Nuevo subtítulo de Devoluciones
                            Text(
                                text = "Devoluciones: ${formatWeight(totalKilosDevueltosActual)} Kg",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.Gray
                            )
                        }

                        // Selector Mes/Año
                        MySegmentedButton(
                            options = listOf("Mes", "Año"),
                            selectedIndex = if (isAnnual) 1 else 0,
                            onSelect = {
                                isAnnual = it == 1
                                updateVentaDataList()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 🔹 Tarjeta del gráfico
                VentaChartCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    ventaDataList = ventaDataList,
                    isAnnual = isAnnual
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
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(ultimasVentas) { venta -> VentaItem(venta) }
        }
    }
}