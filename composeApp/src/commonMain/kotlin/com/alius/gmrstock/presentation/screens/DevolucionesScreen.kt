package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
        // Lógica de control de último cliente: Mapa de BigBagNumber a Nombre de Cliente
        var ultimoClientePorBb by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

        var bigBagsSeleccionados by remember { mutableStateOf<Set<BigBags>>(emptySet()) }
        var showConfirmMultipleDialog by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- Cabecera y Botón Volver 🔀 (SIN CAMBIOS) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                    }
                }

                Text(
                    text = "Devoluciones",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Seleccione lote para devolver",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp)
                )

                // --- Input de Búsqueda (MEJORADO) 🔍 ---
                OutlinedTextField(
                    value = numeroLote,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } || input.isEmpty()) { // Permitir limpiar el campo
                            numeroLote = input
                            clientes = emptyList()
                            clienteSeleccionado = null
                            bigBagsFiltrados = emptyList()
                            loteBuscado = null
                            loteArchivadoTemporal = null
                            bigBagsSeleccionados = emptySet()
                            errorMessage = null // Limpiar mensaje de error al escribir
                        }
                    },
                    label = { Text("Número de lote") },
                    singleLine = true,
                    // ESTÉTICA MEJORADA: Esquinas redondeadas consistentes
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp)) // Aumentado el espacio

                // --- Botón de Búsqueda (MEJORADO) ---
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
                            ultimoClientePorBb = emptyMap() // Limpiar mapa

                            try {
                                // LÓGICA DE BÚSQUEDA (SIN CAMBIOS)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    // ESTÉTICA MEJORADA: Esquinas redondeadas consistentes
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = numeroLote.isNotBlank() && !isLoading
                ) {
                    Text("Buscar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                }

                // --- Selección de cliente y lista de BigBags ---
                if (clientes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp)) // Aumentado el espacio
                    Text("Seleccione cliente:", modifier = Modifier.padding(start = 16.dp))
                    ClienteDropdown(
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
                                    // FILTRO: Status 'o' Y último cliente que lo compró
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

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else if (clienteSeleccionado != null && bigBagsFiltrados.isNotEmpty() && loteBuscado != null) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            "BigBags habilitados para devolución de $clienteSeleccionado",
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryColor.copy(alpha = 0.8f), // Pequeño toque estético
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp) // Más padding
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                            // ESTÉTICA MEJORADA: Espaciado consistente (10dp)
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(bigBagsFiltrados, key = { it.bbNumber }) { bigBag ->
                                BigBagSeleccionableItem(
                                    bigBag = bigBag,
                                    isSelected = bigBagsSeleccionados.contains(bigBag),
                                    onToggleSelect = {
                                        bigBagsSeleccionados = if (bigBagsSeleccionados.contains(bigBag)) {
                                            bigBagsSeleccionados - bigBag
                                        } else {
                                            bigBagsSeleccionados + bigBag
                                        }
                                    }
                                )
                            }
                        }

                        // Botón de Devolución Consolidada (MEJORADO)
                        Button(
                            onClick = { showConfirmMultipleDialog = true },
                            enabled = bigBagsSeleccionados.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            // ESTÉTICA MEJORADA: Esquinas redondeadas consistentes
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text(
                                "Devolver ${bigBagsSeleccionados.size} BigBag(s)",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold // Negrita para el texto del botón
                            )
                        }
                    }

                } else if (clienteSeleccionado != null && bigBagsFiltrados.isEmpty() && loteBuscado != null) {
                    // Mensaje si no hay un error específico ya cargado (que empiece con ❌)
                    if (errorMessage == null || !errorMessage!!.startsWith("❌")) {
                        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No hay BigBags de este lote para $clienteSeleccionado disponibles para devolución.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                errorMessage?.let {
                    // Muestra el mensaje de estado/error (MEJORADO)
                    Text(
                        text = it,
                        color = when {
                            it.startsWith("❌") -> Color.Red
                            it.startsWith("⚠️") -> Color(0xFFFFA500) // Naranja
                            it.startsWith("✅") -> PrimaryColor.copy(alpha = 0.8f) // Verde suave
                            else -> PrimaryColor
                        },
                        fontWeight = FontWeight.SemiBold, // Texto más visible
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp) // Más padding
                    )
                }
            }
        }

        // --- Diálogo de Confirmación con Lógica de Devolución (SIN CAMBIOS) 🔄 ---
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
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                var loteActualizadoEnStock: LoteModel? = null

                                try {
                                    val loteParaActualizar = loteBuscado!!
                                    var loteArchivado = loteArchivadoTemporal

                                    // 1. REPLICACIÓN Y ELIMINACIÓN (SOLO si el lote venía de Historial)
                                    if (loteArchivado != null) {

                                        // 1.1. Cambiar el status del lote archivado a "s" antes de replicar
                                        loteArchivado = loteArchivado.copy(status = "s")

                                        // 1.2. CRÍTICO: Replicar usando la nueva función POST+PATCH.
                                        val newStockId = historialRepository.agregarYLigaroLote(loteArchivado)

                                        if (newStockId != null) {
                                            // 1.3. Actualizar el LoteModel para la siguiente operación (pasando de Historial a Stock)
                                            loteActualizadoEnStock = loteParaActualizar.copy(id = newStockId, status = "s")

                                            // 1.4. Eliminar de 'historial'.
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
                                        // Si ya estaba en Stock, usamos el lote original
                                        loteActualizadoEnStock = loteParaActualizar
                                    }

                                    val loteActivo = loteActualizadoEnStock!! // Usamos el lote que debe estar en Stock

                                    // 2. CREAR REGISTRO DE DEVOLUCIÓN (Usamos el número del lote activo)
                                    val devolucionBigbagsList = selectedBigBags.map { bb ->
                                        DevolucionBigbag(devolucionBbNumber = bb.bbNumber, devolucionBbWeight = bb.bbWeight)
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

                                        // 3. ACTUALIZAR BIGBAGS EN EL LOTE ACTIVO (Usamos el número del lote activo)
                                        val successUpdate = devolucionesRepository.devolverBigBags(
                                            loteNumber = loteActivo.number,
                                            bigBagNumbers = selectedBigBags.map { it.bbNumber }
                                        )

                                        if (successUpdate) {
                                            // 4. Recargar el lote para actualizar la UI
                                            val updatedLote = devolucionesRepository.getLoteByNumber(loteActivo.number)

                                            if (updatedLote != null) {
                                                // Actualizamos el estado principal de la UI
                                                loteBuscado = updatedLote

                                                // Vuelve a filtrar los BigBags
                                                bigBagsFiltrados = updatedLote.bigBag.filter { bb ->
                                                    bb.bbStatus == "o" && ultimoClientePorBb[bb.bbNumber] == clienteSeleccionado
                                                }
                                                bigBagsSeleccionados = emptySet()

                                                // Mensaje final de éxito
                                                val finalMessage = if (loteArchivadoTemporal != null) {
                                                    "✅ Lote revivido y Devolución completa."
                                                } else {
                                                    "✅ Devolución registrada y Stock actualizado."
                                                }

                                                errorMessage = if (errorMessage?.contains("⚠️") == true) {
                                                    "${errorMessage!!}\n$finalMessage"
                                                } else {
                                                    finalMessage
                                                }

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
                                    // Limpiamos el estado de historial después de completar la transacción
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

// --- FUNCIONES AUXILIARES FUERA DE LA CLASE SCREEN (SIN CAMBIOS) ---

@Composable
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
            Text(
                clienteSeleccionado ?: "Mostrar lista de clientes",
                color = if (clienteSeleccionado != null) Color.Black else Color.Gray
            )
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