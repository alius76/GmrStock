package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.ClientGroupSell
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.ClientGroupSellCard
import com.alius.gmrstock.ui.components.GroupClientBottomSheetContent
import kotlinx.coroutines.launch

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
                    modifier = Modifier.fillMaxHeight(0.6f)
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
