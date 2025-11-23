package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getDevolucionRepository
import com.alius.gmrstock.data.getDevolucionesRepository
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.data.getHistorialRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Devolucion
import com.alius.gmrstock.domain.model.DevolucionBigbag
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.components.BigBagSeleccionableItem
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


@OptIn(ExperimentalMaterial3Api::class)
class DevolucionesScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val devolucionRepository = remember(databaseUrl) { getDevolucionRepository(databaseUrl) }
        val devolucionesRepository = remember(databaseUrl) { getDevolucionesRepository(databaseUrl) }
        val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }
        val historialRepository = remember(databaseUrl) { getHistorialRepository(databaseUrl) }

        var numeroLote by remember { mutableStateOf("") }
        var clientes by remember { mutableStateOf<List<String>>(emptyList()) }
        var clienteSeleccionado by remember { mutableStateOf<String?>(null) }
        var bigBagsFiltrados by remember { mutableStateOf<List<BigBags>>(emptyList()) }

        var loteBuscado by remember { mutableStateOf<LoteModel?>(null) }
        var loteArchivadoTemporal by remember { mutableStateOf<LoteModel?>(null) }

        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var ultimoClientePorBb by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        var bigBagsSeleccionados by remember { mutableStateOf<Set<BigBags>>(emptySet()) }
        var showConfirmMultipleDialog by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {

            // --- HEADER FIJO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Devoluciones",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Seleccione lote para devolver",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            val topPadding = 100.dp

            // --- Contenido scrollable usando LazyColumn ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Input Número de Lote ---
                item {
                    OutlinedTextField(
                        value = numeroLote,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } || input.isEmpty()) {
                                numeroLote = input
                                clientes = emptyList()
                                clienteSeleccionado = null
                                bigBagsFiltrados = emptyList()
                                loteBuscado = null
                                loteArchivadoTemporal = null
                                bigBagsSeleccionados = emptySet()
                                errorMessage = null
                            }
                        },
                        label = { Text("Número de lote") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor
                        )
                    )
                }

                // --- Botón Buscar ---
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                clientes = emptyList()
                                clienteSeleccionado = null
                                bigBagsFiltrados = emptyList()
                                loteBuscado = null
                                loteArchivadoTemporal = null
                                bigBagsSeleccionados = emptySet()
                                ultimoClientePorBb = emptyMap()

                                try {
                                    var loteActivo = devolucionesRepository.getLoteByNumber(numeroLote)
                                    if (loteActivo == null) {
                                        val loteArchivado = historialRepository.getLoteHistorialByNumber(numeroLote)
                                        if (loteArchivado != null) {
                                            loteArchivadoTemporal = loteArchivado
                                            loteBuscado = loteArchivado
                                        } else {
                                            errorMessage = "No se encontró el lote."
                                            return@launch
                                        }
                                    } else {
                                        loteBuscado = loteActivo
                                    }

                                    val lote = loteBuscado!!
                                    val ventas = ventaRepository.obtenerVentasPorLote(numeroLote)

                                    if (ventas.isEmpty()) {
                                        errorMessage = "No se encontraron ventas para este lote."
                                        loteBuscado = null
                                        loteArchivadoTemporal = null
                                        return@launch
                                    }

                                    if (lote.bigBag.all { it.bbStatus != "o" }) {
                                        errorMessage = "Todos los BigBags del lote están en stock. No hay devoluciones posibles."
                                        clientes = emptyList()
                                        loteBuscado = null
                                        loteArchivadoTemporal = null
                                        return@launch
                                    }

                                    clientes = ventas.map { it.ventaCliente }.distinct()

                                    val bbUltimoClienteMap = mutableMapOf<String, String>()
                                    ventas.sortedBy { it.ventaFecha ?: Instant.DISTANT_PAST }
                                        .forEach { venta ->
                                            venta.ventaBigbags.forEach { bb ->
                                                bbUltimoClienteMap[bb.ventaBbNumber] = venta.ventaCliente
                                            }
                                        }
                                    ultimoClientePorBb = bbUltimoClienteMap

                                } catch (e: Exception) {
                                    errorMessage = "❌ Error en el proceso de búsqueda: ${e.message}"
                                    loteBuscado = null
                                    loteArchivadoTemporal = null
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        enabled = numeroLote.isNotBlank() && !isLoading
                    ) {
                        Text(
                            "Buscar",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // --- Dropdown Cliente ---
                if (clientes.isNotEmpty()) {
                    item {
                        Text("Seleccione cliente:", modifier = Modifier.padding(start = 16.dp))
                        ClienteDialog(
                            clientes = clientes,
                            clienteSeleccionado = clienteSeleccionado,
                            onClienteSeleccionado = { cliente ->
                                clienteSeleccionado = cliente
                                bigBagsSeleccionados = emptySet()
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val bigbags = loteBuscado?.bigBag ?: emptyList()
                                        bigBagsFiltrados = bigbags.filter { bb ->
                                            bb.bbStatus == "o" && ultimoClientePorBb[bb.bbNumber] == cliente
                                        }
                                        if (bigBagsFiltrados.isEmpty()) {
                                            errorMessage = "No hay BigBags disponibles para devolver para este cliente."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "❌ Error al cargar BigBags: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                }

                // --- Lista de BigBags ---
                if (clienteSeleccionado != null && bigBagsFiltrados.isNotEmpty() && loteBuscado != null) {
                    items(bigBagsFiltrados, key = { it.bbNumber }) { bigBag ->
                        BigBagSeleccionableItem(
                            bigBag = bigBag,
                            isSelected = bigBagsSeleccionados.contains(bigBag),
                            onToggleSelect = {
                                bigBagsSeleccionados =
                                    if (bigBagsSeleccionados.contains(bigBag)) {
                                        bigBagsSeleccionados - bigBag
                                    } else {
                                        bigBagsSeleccionados + bigBag
                                    }
                            }
                        )
                    }

                    // --- Botón Devolver ---
                    item {
                        Button(
                            onClick = { showConfirmMultipleDialog = true },
                            enabled = bigBagsSeleccionados.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text(
                                "Devolver ${bigBagsSeleccionados.size} BigBag(s)",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- Mensajes de Error/Estado ---
                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = when {
                                errorMessage!!.startsWith("❌") -> Color.Red
                                errorMessage!!.startsWith("⚠️") -> Color(0xFFFFA500)
                                errorMessage!!.startsWith("✅") -> PrimaryColor.copy(alpha = 0.8f)
                                else -> PrimaryColor
                            },
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // --- Diálogo Confirmación Devolución ---
            if (showConfirmMultipleDialog && loteBuscado != null && clienteSeleccionado != null) {
                val selectedBigBags = bigBagsSeleccionados.toList()
                val totalWeightNumber = selectedBigBags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }
                val formattedWeight = formatWeight(totalWeightNumber)

                AlertDialog(
                    onDismissRequest = { showConfirmMultipleDialog = false },
                    title = { Text("Confirmar Devolución", fontWeight = FontWeight.Bold, color = PrimaryColor) },
                    text = {
                        Column {
                            Text("Cliente: $clienteSeleccionado", fontWeight = FontWeight.SemiBold)
                            Text("Lote: ${loteBuscado!!.number}")
                            Text("Material: ${loteBuscado!!.description}")
                            Spacer(Modifier.height(8.dp))
                            Text("Cantidad de BigBags: ${selectedBigBags.size}", fontWeight = FontWeight.SemiBold)
                            Text("Peso total a devolver: $formattedWeight Kg")
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmMultipleDialog = false
                                // --- lógica de devolución tal como la tenías ---
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    var loteActualizadoEnStock: LoteModel? = null
                                    try {
                                        val loteParaActualizar = loteBuscado!!
                                        var loteArchivado = loteArchivadoTemporal

                                        if (loteArchivado != null) {
                                            loteArchivado = loteArchivado.copy(status = "s")
                                            val newStockId = historialRepository.agregarYLigaroLote(loteArchivado)
                                            if (newStockId != null) {
                                                loteActualizadoEnStock = loteParaActualizar.copy(id = newStockId, status = "s")
                                                val successDelete = historialRepository.eliminarLoteHistorial(loteArchivado.id)
                                                if (!successDelete) {
                                                    errorMessage = "⚠️ Lote copiado a Stock, pero falló la eliminación del registro de Historial. Revise Historial."
                                                }
                                            } else {
                                                errorMessage = "❌ Error al copiar/ligar lote de Historial a Stock. Cancelando devolución."
                                                isLoading = false
                                                return@launch
                                            }
                                        } else {
                                            loteActualizadoEnStock = loteParaActualizar
                                        }

                                        val loteActivo = loteActualizadoEnStock!!

                                        val devolucionBigbagsList = selectedBigBags.map { bb ->
                                            DevolucionBigbag(bb.bbNumber, bb.bbWeight)
                                        }
                                        val devolucion = Devolucion(
                                            devolucionCliente = clienteSeleccionado!!,
                                            devolucionLote = loteActivo.number,
                                            devolucionMaterial = loteActivo.description,
                                            devolucionFecha = Clock.System.now(),
                                            devolucionPesoTotal = formattedWeight,
                                            devolucionBigbags = devolucionBigbagsList
                                        )
                                        val firestoreSuccess = devolucionRepository.agregarDevolucion(devolucion)

                                        if (firestoreSuccess) {
                                            val successUpdate = devolucionesRepository.devolverBigBags(
                                                loteNumber = loteActivo.number,
                                                bigBagNumbers = selectedBigBags.map { it.bbNumber }
                                            )

                                            if (successUpdate) {
                                                val updatedLote = devolucionesRepository.getLoteByNumber(loteActivo.number)
                                                if (updatedLote != null) {
                                                    loteBuscado = updatedLote
                                                    bigBagsFiltrados = updatedLote.bigBag.filter { bb ->
                                                        bb.bbStatus == "o" && ultimoClientePorBb[bb.bbNumber] == clienteSeleccionado
                                                    }
                                                    bigBagsSeleccionados = emptySet()
                                                    val finalMessage = if (loteArchivadoTemporal != null) {
                                                        "✅ Lote revivido y Devolución completa."
                                                    } else {
                                                        "✅ Devolución registrada y Stock actualizado."
                                                    }
                                                    errorMessage = if (errorMessage?.contains("⚠️") == true) {
                                                        "${errorMessage!!}\n$finalMessage"
                                                    } else finalMessage
                                                } else {
                                                    errorMessage = "✅ Devolución registrada, pero error al recargar el lote."
                                                }
                                            } else {
                                                errorMessage = "❌ Error al actualizar los BigBags en el Lote."
                                            }
                                        } else {
                                            errorMessage = "❌ Error al guardar el registro consolidado de devolución."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "❌ Error en el proceso de devolución: ${e.message}"
                                    } finally {
                                        isLoading = false
                                        loteArchivadoTemporal = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryColor)
                        ) {
                            Text("Confirmar", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmMultipleDialog = false }) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold, color = PrimaryColor)
                        }
                    }
                )
            }
        }
    }

    // --- FUNCIONES AUXILIARES ---
    @Composable
    // Lo dejo pero no lo estoy usando
    fun ClienteDropdown(
        clientes: List<String>,
        clienteSeleccionado: String?,
        onClienteSeleccionado: (String) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
                border = BorderStroke(1.dp, PrimaryColor)
            ) {
                Text(clienteSeleccionado ?: "Mostrar lista de clientes", color = if (clienteSeleccionado != null) Color.Black else Color.Gray)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                clientes.forEach { cliente ->
                    DropdownMenuItem(
                        text = { Text(cliente) },
                        onClick = {
                            expanded = false
                            onClienteSeleccionado(cliente)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClienteDialog(
    clientes: List<String>,
    clienteSeleccionado: String?,
    onClienteSeleccionado: (String) -> Unit
) {
    var showClientesDialog by remember { mutableStateOf(false) }
    var tempSelected by remember { mutableStateOf(clienteSeleccionado) }
    var dialogClientes by remember { mutableStateOf(clientes) } // captura el estado actual de la lista

    // Botón para abrir diálogo
    OutlinedButton(
        onClick = {
            dialogClientes = clientes // captura la lista actual
            showClientesDialog = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
        border = BorderStroke(1.dp, PrimaryColor)
    ) {
        Text(
            text = clienteSeleccionado ?: "Mostrar lista de clientes",
            color = if (clienteSeleccionado != null) Color.Black else Color.Gray
        )
    }

    if (showClientesDialog) {
        Dialog(
            onDismissRequest = {
                showClientesDialog = false
                tempSelected = clienteSeleccionado
            },
            properties = DialogProperties(usePlatformDefaultWidth = false) // fuerza el centrado inicial
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 500.dp), // altura máxima
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .wrapContentHeight() // evita recomposiciones que cambien la altura
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

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ) {
                        LazyColumn {
                            items(dialogClientes) { cliente ->
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
                                    Text(cliente)
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
                            tempSelected = clienteSeleccionado
                        }) { Text("Cancelar", color = PrimaryColor) }

                        Spacer(modifier = Modifier.width(12.dp))

                        TextButton(
                            onClick = {
                                showClientesDialog = false
                                tempSelected?.let { onClienteSeleccionado(it) }
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
