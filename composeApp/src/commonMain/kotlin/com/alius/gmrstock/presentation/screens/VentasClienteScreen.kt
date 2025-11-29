package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.CertificadoRepository
import com.alius.gmrstock.data.CertificadoRepositoryImpl
import com.alius.gmrstock.data.getCertificadoRepository
import com.alius.gmrstock.data.getClientRepository
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.CertificadoStatus
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.VentaBigBagsDialogContent
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        val certificadoRepository = remember(databaseUrl) { getCertificadoRepository(databaseUrl) }

        var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
        var showClientsLoading by remember { mutableStateOf(true) }
        var clientsError by remember { mutableStateOf<String?>(null) }

        var selectedClient by remember { mutableStateOf<Cliente?>(null) }
        var showClientesDialog by remember { mutableStateOf(false) }

        var ventas by remember { mutableStateOf<List<Venta>>(emptyList()) }
        var certificadosMap by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
        var ventasLoading by remember { mutableStateOf(false) }
        var ventasError by remember { mutableStateOf<String?>(null) }
        var totalKilos by remember { mutableStateOf(0.0) }

        // --- Cargar clientes ---
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

        // --- Cargar ventas y certificados ---
        fun loadVentasForClient(cliente: Cliente) {
            scope.launch {
                ventasLoading = true
                ventasError = null
                try {
                    val result = ventaRepository.mostrarVentasPorCliente(cliente.cliNombre)
                        .sortedByDescending { it.ventaFecha ?: kotlinx.datetime.Instant.DISTANT_PAST }

                    ventas = result
                    totalKilos = ventas.sumOf { it.ventaPesoTotal?.toDoubleOrNull() ?: 0.0 }

                    // Precargar certificados en paralelo para que los colores aparezcan de inmediato
                    val certificados = result.map { venta ->
                        async { certificadoRepository.getCertificadoByLoteNumber(venta.ventaLote) }
                    }.awaitAll()

                    certificadosMap = result.mapIndexed { index, venta ->
                        venta.ventaLote to certificados[index]
                    }.toMap()

                } catch (e: Exception) {
                    ventasError = e.message ?: "Error cargando ventas"
                    ventas = emptyList()
                    certificadosMap = emptyMap()
                    totalKilos = 0.0
                } finally {
                    ventasLoading = false
                }
            }
        }

        // --- UI ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            // --- HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

            val topPadding = 160.dp

            // --- Contenido scrollable ---
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

                else -> selectedClient?.let { cliente ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = topPadding),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(ventas) { venta ->
                            VentaRow(
                                venta = venta,
                                certificado = certificadosMap[venta.ventaLote],
                                cliente = cliente
                            )
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // --- Diálogo de selección de clientes ---
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
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                LazyColumn {
                                    items(clients) { clienteItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { tempSelected = clienteItem }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = tempSelected == clienteItem,
                                                onClick = { tempSelected = clienteItem },
                                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(clienteItem.cliNombre)
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
    private fun VentaRow(
        venta: Venta,
        certificado: Certificado?,
        cliente: Cliente
    ) {
        var showCertificadoDialog by remember { mutableStateOf(false) }
        var showBigbagsDialog by remember { mutableStateOf(false) }

        val certificadoIconColor = when (certificado?.status) {
            CertificadoStatus.CORRECTO -> PrimaryColor
            CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        // --- Card con fondo transparente y borde ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable(
                    onClick = { /* nada */ },
                    indication = rememberRipple(
                        bounded = true,
                        color = PrimaryColor.copy(alpha = 0.2f)
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Información de la venta ---
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatInstant(venta.ventaFecha ?: kotlinx.datetime.Instant.DISTANT_PAST),
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = venta.ventaMaterial.ifEmpty { "—" },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Lote: ${venta.ventaLote.ifEmpty { "—" }}",
                        color = TextSecondary
                    )
                }

                // --- Peso total ---
                Text(
                    text = venta.ventaPesoTotal?.let { "${formatWeight(it.toDoubleOrNull() ?: 0.0)} Kg" } ?: "—",
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // --- Botones de acción ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showCertificadoDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Ver Certificado",
                            tint = certificadoIconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showBigbagsDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Ver BigBags",
                            tint = PrimaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // --- Diálogo de certificado ---
        if (showCertificadoDialog) {
            Dialog(onDismissRequest = { showCertificadoDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val (icon, estadoText, estadoColor) = when (certificado?.status) {
                            CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
                            CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
                            else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Icon(icon, contentDescription = estadoText, tint = estadoColor, modifier = Modifier.size(48.dp))
                        Text(
                            "Certificado de ${venta.ventaLote}",
                            color = PrimaryColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        certificado?.parametros?.forEach { parametro ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(parametro.descripcion, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (parametro.warning) {
                                            Icon(Icons.Default.Warning, contentDescription = "Advertencia", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(parametro.valor, color = if (parametro.warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                    }
                                }

                                val rangoTexto = parametro.rango?.let { rango ->
                                    if (rango.valorMin != null && rango.valorMax != null) {
                                        val min = if (rango.valorMin % 1.0 == 0.0) rango.valorMin.toInt() else rango.valorMin
                                        val max = if (rango.valorMax % 1.0 == 0.0) rango.valorMax.toInt() else rango.valorMax
                                        "Rango: ($min - $max ${parametro.unidad})"
                                    } else "Rango: N/A"
                                } ?: "Rango: N/A"
                                Text(rangoTexto, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }

                        if (certificado == null) {
                            Text("No se encontraron datos del certificado.", color = Color.Gray, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showCertificadoDialog = false }, modifier = Modifier.align(Alignment.End)) {
                            Text("Cerrar", color = PrimaryColor)
                        }
                    }
                }
            }
        }

        // --- Diálogo de BigBags ---
        if (showBigbagsDialog) {
            AlertDialog(
                onDismissRequest = { showBigbagsDialog = false },
                confirmButton = {
                    TextButton(onClick = { showBigbagsDialog = false }) {
                        Text("Cerrar", color = PrimaryColor)
                    }
                },
                title = { Text("Lista de BigBags", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                text = { VentaBigBagsDialogContent(bigBags = venta.ventaBigbags) }
            )
        }
    }


}
