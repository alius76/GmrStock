package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.components.ReservaCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ReservasScreen(private val databaseUrl: String) : Screen {

    // 1. Definición de opciones de ordenación
    private enum class OrderOption(val label: String, val field: String) {
        CLIENTE_ASC("Cliente (A-Z)", "booked"),
        CLIENTE_DESC("Cliente (Z-A)", "booked"),
        FECHA_ASC("Fecha (Antiguos)", "dateBooked"),
        FECHA_DESC("Fecha (Nuevos)", "dateBooked")
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { getLoteRepository(databaseUrl) }

        var reservas by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var totalKilos by remember { mutableStateOf(0.0) }

        var selectedOrder by remember { mutableStateOf(OrderOption.CLIENTE_ASC) }
        var showSortMenu by remember { mutableStateOf(false) }

        val loadReservas: () -> Unit = {
            scope.launch {
                try {
                    isLoading = true
                    val direction = if (selectedOrder.label.contains("A-Z") || selectedOrder.label.contains("Antiguos"))
                        "ASCENDING" else "DESCENDING"
                    val data = repository.listarLotesReservados(
                        orderBy = selectedOrder.field,
                        direction = direction
                    )
                    val filteredData = data.filter { it.booked?.cliNombre != "NO OK" }
                    reservas = filteredData
                    totalKilos = reservas.sumOf { it.totalWeight.toDoubleOrNull() ?: 0.0 }
                } catch (e: Exception) {
                    println("Error al cargar reservas: ${e.message}")
                    reservas = emptyList()
                } finally {
                    isLoading = false
                }
            }
        }

        LaunchedEffect(selectedOrder) { loadReservas() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(horizontal = 16.dp)
        ) {

            // --- HEADER FIJO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
                    .zIndex(1f)
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                    }
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Lotes reservados",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // --- Dropdown de ordenación ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(selectedOrder.label, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Ordenar")
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        OrderOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    selectedOrder = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                Divider(color = Color.LightGray.copy(alpha = 0.5f))
            }

            // --- CUERPO DE LA PANTALLA ---
            val topPadding = 160.dp // espacio para el header fijo
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (reservas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No hay lotes reservados en este momento.",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reservas) { lote ->
                        ReservaCard(lote = lote, onClick = {
                            println("Lote ${it.number} clickeado para ver detalle")
                        })
                    }
                }
            }
        }
    }
}
