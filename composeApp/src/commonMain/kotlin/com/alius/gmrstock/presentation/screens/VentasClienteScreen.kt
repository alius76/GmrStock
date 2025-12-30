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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.PdfGenerator
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getCertificadoRepository
import com.alius.gmrstock.data.getClientRepository
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.* import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
class VentasClienteScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Repositorios
        val clientRepository = remember(databaseUrl) { getClientRepository(databaseUrl) }
        val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }
        val certificadoRepository = remember(databaseUrl) { getCertificadoRepository(databaseUrl) }

        // --- Estados ---
        var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
        var selectedClient by remember { mutableStateOf<Cliente?>(null) }
        var isFirstLoad by remember { mutableStateOf(true) }
        var showClientesDialog by remember { mutableStateOf(false) }

        val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
        var startDate by remember { mutableStateOf(LocalDate(today.year, today.monthNumber, 1)) }
        var endDate by remember { mutableStateOf(today) }
        var showStartPicker by remember { mutableStateOf(false) }
        var showEndPicker by remember { mutableStateOf(false) }

        var ventasRaw by remember { mutableStateOf<List<Venta>>(emptyList()) }
        var certificadosMap by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
        var isLoading by remember { mutableStateOf(false) }
        var totalKilosGlobal by remember { mutableStateOf(0.0) }

        // Lógica de carga
        fun loadData() {
            scope.launch {
                isLoading = true
                isFirstLoad = false
                try {
                    val inicioInstant = startDate.atStartOfDayIn(TimeZone.currentSystemDefault())
                    val finInstant = endDate.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault())

                    val result = ventaRepository.obtenerVentasReporteGlobal(
                        cliente = selectedClient?.cliNombre,
                        inicio = inicioInstant,
                        fin = finInstant
                    )

                    ventasRaw = result
                    totalKilosGlobal = result.sumOf { it.ventaPesoTotal?.toDoubleOrNull() ?: 0.0 }

                    val certs = result.map { v ->
                        async { certificadoRepository.getCertificadoByLoteNumber(v.ventaLote) }
                    }.awaitAll()
                    certificadosMap = result.mapIndexed { i, v -> v.ventaLote to certs[i] }.toMap()
                } catch (e: Exception) {
                    ventasRaw = emptyList()
                    totalKilosGlobal = 0.0
                } finally {
                    isLoading = false
                }
            }
        }

        LaunchedEffect(databaseUrl) {
            try {
                clients = clientRepository.getAllClientsOrderedByName().filter { it.cliNombre != "NO OK" }
            } catch (e: Exception) {}
        }

        LaunchedEffect(startDate, endDate, selectedClient) {
            if (!isFirstLoad) loadData()
        }

        val ventasPorMaterial = remember(ventasRaw) {
            ventasRaw.groupBy { it.ventaMaterial ?: "General" }
                .mapValues { entry -> entry.value.sumOf { it.ventaPesoTotal?.toDoubleOrNull() ?: 0.0 } }
        }
        val ventasMensuales = remember(ventasRaw) { agruparVentasPorMes(ventasRaw) }

        Scaffold(
            containerColor = BackgroundColor,
            topBar = {
                VentasTopSection(
                    clientName = if (isFirstLoad) "Seleccionar Cliente" else selectedClient?.cliNombre ?: "TODOS LOS CLIENTES",
                    totalKilos = totalKilosGlobal,
                    startDate = startDate,
                    endDate = endDate,
                    // Pasamos los estados necesarios para el PDF y validaciones
                    isFirstLoad = isFirstLoad,
                    hasVentas = ventasRaw.isNotEmpty(),
                    onBack = { navigator.pop() },
                    onSelectClient = { showClientesDialog = true },
                    onSelectStart = { showStartPicker = true },
                    onSelectEnd = { showEndPicker = true },
                    onPrint = {
                        val startStr = "${startDate.dayOfMonth.toString().padStart(2, '0')}/${startDate.monthNumber.toString().padStart(2, '0')}"
                        val endStr = "${endDate.dayOfMonth.toString().padStart(2, '0')}/${endDate.monthNumber.toString().padStart(2, '0')}"

                        PdfGenerator.generateVentasReportPdf(
                            clienteNombre = selectedClient?.cliNombre ?: "TODOS LOS CLIENTES",
                            ventas = ventasRaw,
                            totalKilos = totalKilosGlobal,
                            dateRange = "$startStr al $endStr",
                            desgloseMateriales = ventasPorMaterial
                        )
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center), color = PrimaryColor)
                } else if (isFirstLoad) {
                    EmptyStateVentas(isNoClient = true)
                } else if (ventasRaw.isEmpty()) {
                    EmptyStateVentas(isNoClient = false)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            VentasKPISection(
                                totalKilos = totalKilosGlobal,
                                totalVentas = ventasRaw.size,
                                desgloseMateriales = ventasPorMaterial
                            )
                        }

                        item {
                            Text(
                                text = "Desglose mensual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(ventasMensuales) { mesData ->
                            val porcentaje = if (totalKilosGlobal > 0) (mesData.totalKilos / totalKilosGlobal).toFloat() else 0f
                            ExpandableMesCard(
                                mesData = mesData,
                                porcentaje = porcentaje,
                                certificadosMap = certificadosMap
                            )
                        }

                        item { Spacer(Modifier.height(40.dp)) }
                    }
                }
            }

            // Diálogos
            if (showClientesDialog) {
                ClientesSelectedDialogContent(
                    clients = clients,
                    currentSelectedClient = selectedClient,
                    showAllOption = true,
                    onDismiss = { showClientesDialog = false },
                    onConfirm = { cliente ->
                        selectedClient = cliente
                        if (isFirstLoad) loadData()
                        showClientesDialog = false
                    }
                )
            }

            if (showStartPicker) {
                UniversalDatePickerDialog(initialDate = startDate, onDateSelected = { startDate = it }, onDismiss = { showStartPicker = false })
            }

            if (showEndPicker) {
                UniversalDatePickerDialog(initialDate = endDate, onDateSelected = { endDate = it }, onDismiss = { showEndPicker = false })
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun VentasTopSection(
    clientName: String,
    totalKilos: Double,
    startDate: LocalDate,
    endDate: LocalDate,
    isFirstLoad: Boolean,
    hasVentas: Boolean,
    onBack: () -> Unit,
    onSelectClient: () -> Unit,
    onSelectStart: () -> Unit,
    onSelectEnd: () -> Unit,
    onPrint: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(BackgroundColor).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                }
                Column(Modifier.padding(start = 8.dp)) {
                    Text("Ventas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Total: ${formatWeight(totalKilos)} kg", fontSize = 14.sp, color = Color.Gray)
                }
            }
            IconButton(
                onClick = onPrint,
                enabled = !isFirstLoad && hasVentas
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Imprimir",
                    tint = if (!isFirstLoad && hasVentas) PrimaryColor else Color.Gray
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(1.dp, PrimaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .clickable { onSelectClient() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    text = clientName,
                    color = if (clientName != "Seleccionar Cliente") PrimaryColor else Color.Gray,
                    fontWeight = if (clientName != "Seleccionar Cliente") FontWeight.Medium else FontWeight.Normal
                )
                Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        DateRangeFilter(
            startDate = startDate,
            endDate = endDate,
            onSelectStartDate = onSelectStart,
            onSelectEndDate = onSelectEnd
        )
    }
}

// --- MODELOS Y LÓGICA ---

data class VentasMes(
    val mesLabel: String,
    val totalKilos: Double,
    val listaVentas: List<Venta>,
    val fechaReferencia: LocalDate
)

private fun agruparVentasPorMes(ventas: List<Venta>): List<VentasMes> {
    val mesesEspanol = mapOf(
        1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril",
        5 to "Mayo", 6 to "Junio", 7 to "Julio", 8 to "Agosto",
        9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre"
    )

    return ventas.filter { it.ventaFecha != null }
        .groupBy {
            val d = it.ventaFecha!!.toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${mesesEspanol[d.monthNumber]} ${d.year}"
        }.map { (label, list) ->
            VentasMes(
                mesLabel = label,
                totalKilos = list.sumOf { it.ventaPesoTotal?.toDoubleOrNull() ?: 0.0 },
                listaVentas = list.sortedByDescending { it.ventaFecha },
                fechaReferencia = list.first().ventaFecha!!.toLocalDateTime(TimeZone.currentSystemDefault()).date
            )
        }.sortedByDescending { it.fechaReferencia }
}

@Composable
fun EmptyStateVentas(isNoClient: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isNoClient) Icons.Default.Search else Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp).alpha(0.2f),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isNoClient) "Seleccione un cliente para ver su historial" else "No se encontraron ventas en este periodo",
            style = MaterialTheme.typography.bodyLarge, color = Color.Gray, fontWeight = FontWeight.Medium
        )
    }
}