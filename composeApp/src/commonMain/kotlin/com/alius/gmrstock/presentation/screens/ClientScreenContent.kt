package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.ClientGroupSell
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.ClientGroupSellCard
import com.alius.gmrstock.ui.components.GroupClientBottomSheetContent
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClientScreenContent(user: User, databaseUrl: String) {
    val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }
    var ventas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var selectedClientGroup by remember { mutableStateOf<ClientGroupSell?>(null) }
    var selectedClientVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(databaseUrl) {
        loading = true
        ventas = ventaRepository.mostrarVentasDelMes()
        loading = false
    }

    // Mapeo de meses en español
    val monthNamesEs = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // Obtener mes actual multiplataforma
    val currentMonth = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        monthNamesEs[now.monthNumber - 1] // monthNumber es 1..12
    }

    // Agrupar ventas por cliente y calcular total kilos
    val grouped: List<Pair<ClientGroupSell, List<Venta>>> = ventas
        .groupBy { it.ventaCliente }
        .map { (clienteNombre, ventasCliente) ->
            val totalKilos = ventasCliente.sumOf { venta ->
                venta.ventaBigbags.sumOf { it.ventaBbWeight.toIntOrNull() ?: 0 }
            }

            ClientGroupSell(
                cliente = com.alius.gmrstock.domain.model.Cliente(cliNombre = clienteNombre),
                totalVentasMes = ventasCliente.size,
                totalKilosVendidos = totalKilos.toString()
            ) to ventasCliente
        }
        .sortedBy { it.first.cliente.cliNombre.lowercase() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Encabezado con título
                item {
                    Spacer(modifier = Modifier.height(20.dp)) // espacio específico arriba del título
                    Text(
                        text = "Clientes activos en $currentMonth",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(grouped) { group ->
                    ClientGroupSellCard(group = group.first) { clickedGroup ->
                        selectedClientGroup = clickedGroup
                        selectedClientVentas =
                            grouped.find { it.first == clickedGroup }?.second ?: emptyList()

                        coroutineScope.launch {
                            bottomSheetState.show()
                        }
                    }
                }
            }

            // BottomSheet usando ModalBottomSheet
            if (selectedClientGroup != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch { bottomSheetState.hide() }
                        selectedClientGroup = null
                    },
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxHeight(0.45f)
                ) {
                    GroupClientBottomSheetContent(
                        cliente = selectedClientGroup!!.cliente,
                        ventas = selectedClientVentas,
                        onDismissRequest = {
                            coroutineScope.launch { bottomSheetState.hide() }
                            selectedClientGroup = null
                        }
                    )
                }
            }
        }
    }
}