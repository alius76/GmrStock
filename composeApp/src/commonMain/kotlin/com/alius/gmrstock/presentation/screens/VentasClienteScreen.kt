package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getClientRepository
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class VentasClienteScreen(
    private val databaseUrl: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        val clientRepository = remember(databaseUrl) { getClientRepository(databaseUrl) }
        val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }

        var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
        var showClientsLoading by remember { mutableStateOf(true) }
        var clientsError by remember { mutableStateOf<String?>(null) }

        var selectedClient by remember { mutableStateOf<Cliente?>(null) }
        var showClientesDialog by remember { mutableStateOf(false) }

        var ventas by remember { mutableStateOf<List<Venta>>(emptyList()) }
        var ventasLoading by remember { mutableStateOf(false) }
        var ventasError by remember { mutableStateOf<String?>(null) }
        var totalKilos by remember { mutableStateOf(0.0) }

        // Cargar clientes
        LaunchedEffect(databaseUrl) {
            showClientsLoading = true
            clientsError = null
            try {
                val allClients = clientRepository.getAllClientsOrderedByName()
                clients = allClients.filter { it.cliNombre != "NO OK" }
            } catch (e: Exception) {
                clientsError = e.message ?: "Error cargando clientes"
                clients = emptyList()
            } finally {
                showClientsLoading = false
            }
        }

        fun loadVentasForClient(cliente: Cliente) {
            scope.launch {
                ventasLoading = true
                ventasError = null
                try {
                    val result = ventaRepository.mostrarVentasPorCliente(cliente.cliNombre)
                        .sortedByDescending { it.ventaFecha ?: kotlinx.datetime.Instant.DISTANT_PAST }
                    ventas = result
                    totalKilos = ventas.sumOf { it.ventaPesoTotal?.toDoubleOrNull() ?: 0.0 }
                } catch (e: Exception) {
                    ventasError = e.message ?: "Error cargando ventas"
                    ventas = emptyList()
                    totalKilos = 0.0
                } finally {
                    ventasLoading = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            // 🔹 HEADER FIJO: flecha + título + subtítulo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            text = "Ventas por cliente",
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

                Spacer(modifier = Modifier.height(12.dp))

                // --- Caja de cliente ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = if (selectedClient != null) PrimaryColor else TextSecondary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .clickable { showClientesDialog = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = selectedClient?.cliNombre ?: "Seleccione un cliente",
                        color = if (selectedClient != null) PrimaryColor else TextSecondary
                    )
                }

                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)
            }

            val topPadding = 160.dp // altura aproximada del header + caja de cliente

            // 🔹 Contenido scrollable
            when {
                ventasLoading -> Box(
                    modifier = Modifier.fillMaxSize().padding(top = topPadding),
                    contentAlignment = Alignment.TopCenter
                ) { CircularProgressIndicator(color = PrimaryColor) }

                ventasError != null -> Box(
                    modifier = Modifier.fillMaxSize().padding(top = topPadding),
                    contentAlignment = Alignment.TopCenter
                ) { Text(ventasError ?: "Error desconocido", color = Color.Red) }

                ventas.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(top = topPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp).alpha(0.6f))
                        Spacer(Modifier.height(12.dp))
                        Text("No se encontraron ventas para este cliente.", color = Color.DarkGray)
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = topPadding),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(ventas) { venta ->
                        VentaRow(venta)
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }

            // 🔹 Diálogo de selección de clientes
            if (showClientesDialog) {
                var tempSelected by remember { mutableStateOf(selectedClient) }

                Dialog(onDismissRequest = {
                    showClientesDialog = false
                    tempSelected = selectedClient
                }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .heightIn(max = 500.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = "Seleccione un cliente",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PrimaryColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                LazyColumn {
                                    items(clients) { cliente ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { tempSelected = cliente }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = tempSelected == cliente,
                                                onClick = { tempSelected = cliente },
                                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(cliente.cliNombre)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    showClientesDialog = false
                                    tempSelected = selectedClient
                                }) { Text("Cancelar", color = PrimaryColor) }

                                Spacer(modifier = Modifier.width(12.dp))

                                TextButton(
                                    onClick = {
                                        showClientesDialog = false
                                        selectedClient = tempSelected
                                        selectedClient?.let { loadVentasForClient(it) }
                                    },
                                    enabled = tempSelected != null
                                ) {
                                    Text(
                                        "Aceptar",
                                        color = if (tempSelected != null) PrimaryColor
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun VentaRow(venta: Venta) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatInstant(venta.ventaFecha ?: kotlinx.datetime.Instant.DISTANT_PAST),
                    fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Text(venta.ventaMaterial.ifEmpty { "—" },
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text("Lote: ${venta.ventaLote.ifEmpty { "—" }}", color = TextSecondary)
            }
            Text(
                venta.ventaPesoTotal?.let { "${formatWeight(it.toDoubleOrNull() ?: 0.0)} Kg" } ?: "—",
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryColor
            )
        }
    }
}
