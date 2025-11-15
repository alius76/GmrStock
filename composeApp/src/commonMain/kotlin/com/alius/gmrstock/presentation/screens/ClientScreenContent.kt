package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClientScreenContent(user: User, databaseUrl: String) {
    val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }
    var ventas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var selectedClientGroup by remember { mutableStateOf<ClientGroupSell?>(null) }
    var selectedClientVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(databaseUrl) {
        loading = true
        ventas = ventaRepository.mostrarVentasDelMes()
        loading = false
    }

    val monthNamesEs = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val currentMonth = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        monthNamesEs[now.monthNumber - 1]
    }

    val grouped: List<Pair<ClientGroupSell, List<Venta>>> = ventas
        .groupBy { it.ventaCliente }
        .map { (clienteNombre, ventasCliente) ->

            // 1. Calcular Kilos (Simplificamos ya que ahora sabemos que 'ventaPesoTotal' es el que se usa y puede ser Int/String)
            val totalKilos = ventasCliente.sumOf { venta ->

                venta.ventaPesoTotal?.toIntOrNull()
                    ?: venta.ventaBigbags.sumOf { it.ventaBbWeight.toIntOrNull() ?: 0 }
            }

            // 2. Calcular BigBags: Suma el tamaño de la lista de BigBags de cada venta
            val totalBigBags = ventasCliente.sumOf { it.ventaBigbags.size } // ⬅️ Nuevo cálculo

            ClientGroupSell(
                cliente = com.alius.gmrstock.domain.model.Cliente(cliNombre = clienteNombre),
                totalVentasMes = ventasCliente.size,
                totalKilosVendidos = totalKilos, // Usamos el cálculo de Kilos
                totalBigBags = totalBigBags // ⬅️ Asignamos el valor calculado
            ) to ventasCliente
        }
        .sortedByDescending { it.first.totalKilosVendidos }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = com.alius.gmrstock.ui.theme.PrimaryColor
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- Título y subtítulo ---
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(50.dp))

                        Text(
                            text = "Top clientes en $currentMonth",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Número de clientes: ${grouped.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                // --- Cards de clientes ---
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

            // --- BottomSheet con detalle ---
            selectedClientGroup?.let { clientGroup ->
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch { bottomSheetState.hide() }
                        selectedClientGroup = null
                    },
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxHeight(0.7f)
                ) {
                    GroupClientBottomSheetContent(
                        cliente = clientGroup.cliente,
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
