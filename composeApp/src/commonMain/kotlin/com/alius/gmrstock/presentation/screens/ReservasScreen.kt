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

        // Estado para la ordenación y la lista de datos
        var reservas by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var totalKilos by remember { mutableStateOf(0.0) }

        // 2. Estado del control de ordenación
        var selectedOrder by remember { mutableStateOf(OrderOption.CLIENTE_ASC) }
        var showSortMenu by remember { mutableStateOf(false) }

        // Función para cargar datos basada en la opción de ordenación
        val loadReservas: () -> Unit = {
            scope.launch {
                try {
                    isLoading = true

                    // Lógica para obtener el campo y la dirección de ordenación
                    val direction = if (selectedOrder.label.contains("A-Z") || selectedOrder.label.contains("Antiguos"))
                        "ASCENDING" else "DESCENDING"

                    val data = repository.listarLotesReservados(
                        orderBy = selectedOrder.field,
                        direction = direction
                    )

                    // =======================================================
                    // === FILTRO DE EXCLUSIÓN: ELIMINAR RESERVAS "NO OK" ===
                    // =======================================================
                    val filteredData = data.filter { lote ->
                        // Excluir lotes donde el cliente reservado sea "NO OK"
                        lote.booked?.cliNombre != "NO OK"
                    }

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

        // 3. Efecto de lanzamiento que se ejecuta cuando cambia la ordenación
        LaunchedEffect(selectedOrder) {
            loadReservas()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(horizontal = 16.dp) // Aplicamos padding horizontal al Box principal
        ) {

            // --- HEADER FIJO (Siempre Visible) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor) // Asegurar que el fondo del header tape lo de abajo al hacer scroll
                    .zIndex(1f) // Asegurar que esté por encima del LazyColumn
            ) {
                // Flecha de navegación y título
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

                // Título
                Text(
                    text = "Lotes reservados",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Total Kilos
                Text(
                    text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // 4. Dropdown de Ordenación (Control profesional)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(selectedOrder.label, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Ordenar")
                    }

                    // Menú desplegable
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


            // --- 2. CUERPO (Carga, Vacío o Lista) ---
            val topPadding = 200.dp // Espacio para evitar superposición con el Header fijo

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
                        .padding(top = topPadding), // Aplica el padding superior aquí
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- LISTA DE RESERVAS ---
                    items(reservas) { lote ->
                        ReservaCard(lote = lote, onClick = {
                            // TODO: Implementar navegación al detalle del lote
                            println("Lote ${it.number} clickeado para ver detalle")
                        })
                    }
                }
            }
        }
    }
}