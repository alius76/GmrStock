package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstock.domain.model.*
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.ReservedColor
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.ClientRepository
import com.alius.gmrstock.data.getLoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import com.alius.gmrstock.data.getComandaRepository
import com.alius.gmrstock.domain.model.Comanda
import androidx.compose.ui.draw.clip
import com.alius.gmrstock.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun LoteCard(
    lote: LoteModel,
    certificado: Certificado?,
    certificadoIconColor: Color,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onViewBigBags: (List<BigBags>) -> Unit,
    databaseUrl: String,
    onRemarkUpdated: (LoteModel) -> Unit,
    clientRepository: ClientRepository,
    currentUserEmail: String
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showReservedDialog by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showAddRemarkDialog by remember { mutableStateOf(false) }

    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0

    var currentRemarkText by remember { mutableStateOf(lote.remark) }
    var currentBookedRemark by remember { mutableStateOf(lote.bookedRemark ?: "") }

    val loteRepository = remember { getLoteRepository(databaseUrl) }
    val hasRemark = lote.remark.isNotBlank()

    // -------------------- CARD --------------------
    Card(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {

            // 1. CABECERA REESTRUCTURADA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reservado
                    IconButton(
                        onClick = {
                            currentBookedRemark = lote.bookedRemark ?: ""
                            showReservedDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Ver reservado",
                            tint = PrimaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Observación
                    IconButton(
                        onClick = {
                            currentRemarkText = lote.remark
                            if (hasRemark) showRemarkDialog = true
                            else showAddRemarkDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (hasRemark) Icons.Default.Description else Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = if (hasRemark) "Ver/Editar observación" else "Añadir observación",
                            tint = if (hasRemark) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Certificado
                    IconButton(
                        onClick = { showCertificadoDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Ver certificado",
                            tint = certificadoIconColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // BigBags
                    IconButton(
                        onClick = { showBigBagsDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Ver BigBags",
                            tint = PrimaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 2. BLOQUE DE DETALLES
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Material", lote.description)
                    DetailRow("Fecha", formatInstant(lote.date))
                    DetailRow("Ubicación", lote.location)
                    DetailRow("BigBags", lote.count.toString())
                    DetailRow("Peso total", "${formatWeight(totalWeightNumber)} Kg", PrimaryColor)
                }

                if (lote.booked != null && lote.booked.cliNombre.isNotBlank()) {
                    Surface(
                        color = ReservedColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(start = 8.dp)
                            .width(90.dp)
                            .height(48.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "RESERVADO",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                            Text(
                                text = lote.booked.cliNombre,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }

    // --- InfoCard auxiliar ---
    @Composable
    fun InfoCard(label: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
    }

    // --- Diálogo Observación General ---
    if (showRemarkDialog) {
        val isChanged = currentRemarkText.trim() != lote.remark.trim()
        AlertDialog(
            onDismissRequest = { showRemarkDialog = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Observación del Lote", fontWeight = FontWeight.Bold, color = PrimaryColor, textAlign = TextAlign.Center)
                        Text(lote.number, fontWeight = FontWeight.ExtraBold, color = PrimaryColor, textAlign = TextAlign.Center)
                    }
                }
            },
            text = {
                OutlinedTextField(
                    value = currentRemarkText,
                    onValueChange = { currentRemarkText = it },
                    label = { Text("Editar observación") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor,
                    )
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            showRemarkDialog = false
                            scope.launch {
                                val success = loteRepository.updateLoteRemark(lote.id, "")
                                if (success) onRemarkUpdated(lote.copy(remark = ""))
                                snackbarHostState.showSnackbar(if (success) "Observación eliminada" else "Error al eliminar la observación")
                            }
                        },
                        enabled = lote.remark.isNotBlank()
                    ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }

                    Row(horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRemarkDialog = false }) { Text("Cerrar", color = PrimaryColor) }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                showRemarkDialog = false
                                val remarkToSave = currentRemarkText.trim()
                                scope.launch {
                                    val success = loteRepository.updateLoteRemark(lote.id, remarkToSave)
                                    if (success) onRemarkUpdated(lote.copy(remark = remarkToSave))
                                    snackbarHostState.showSnackbar(if (success) "Observación actualizada" else "Error al actualizar la observación")
                                }
                            },
                            enabled = isChanged && currentRemarkText.isNotBlank()
                        ) { Text("Guardar", color = PrimaryColor) }
                    }
                }
            }
        )
    }

    // --- Diálogo Añadir Observación ---
    if (showAddRemarkDialog) {
        AlertDialog(
            onDismissRequest = { showAddRemarkDialog = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Observación del Lote", fontWeight = FontWeight.Bold, color = PrimaryColor, textAlign = TextAlign.Center)
                        Text(lote.number, fontWeight = FontWeight.Bold, color = PrimaryColor, textAlign = TextAlign.Center)
                    }
                }
            },
            text = {
                OutlinedTextField(
                    value = currentRemarkText,
                    onValueChange = { currentRemarkText = it },
                    label = { Text("Escribe tu observación") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddRemarkDialog = false
                        val remarkToSave = currentRemarkText.trim()
                        scope.launch {
                            val success = loteRepository.updateLoteRemark(lote.id, remarkToSave)
                            if (success) onRemarkUpdated(lote.copy(remark = remarkToSave))
                            snackbarHostState.showSnackbar(if (success) "Observación guardada" else "Error al guardar la observación")
                        }
                    },
                    enabled = currentRemarkText.isNotBlank()
                ) { Text("Guardar", color = PrimaryColor) }
            },
            dismissButton = {
                TextButton(onClick = { showAddRemarkDialog = false }) { Text("Cancelar", color = PrimaryColor) }
            }
        )
    }

    // --- Diálogo BigBags ---
    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) { Text("Cerrar", color = PrimaryColor) }
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Lista de BigBags", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { BigBagsDialogContent(bigBags = lote.bigBag) }
        )
    }

    // --- Diálogo Certificado ---
    if (showCertificadoDialog) {
        Dialog(onDismissRequest = { showCertificadoDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, estadoText, estadoColor) = if (certificado != null) {
                        when (certificado.status) {
                            CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
                            CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
                            else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Icon(icon, contentDescription = estadoText, tint = estadoColor, modifier = Modifier.size(48.dp))
                    Text(
                        "Certificado de ${lote.number}",
                        color = PrimaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (certificado != null) {
                            certificado.parametros.forEach { parametro ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            parametro.descripcion,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            if (parametro.warning) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = "Advertencia",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                parametro.valor,
                                                color = if (parametro.warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.sp
                                            )
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
                        } else {
                            // Mensaje cuando no existe certificado
                            Text(
                                "No se encontraron datos del certificado.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showCertificadoDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar", color = PrimaryColor)
                    }
                }
            }
        }
    }

// --- Diálogo de reservas con selección de cliente ---
    if (showReservedDialog) {
        var selectedCliente by remember { mutableStateOf(lote.booked) }
        var showClientesDialog by remember { mutableStateOf(false) }
        var userToSave by remember { mutableStateOf(currentUserEmail) }
        var clientesList by remember { mutableStateOf<List<Cliente>?>(null) }

        // 🆕 ESTADOS PARA COMANDAS
        var selectedComanda by remember { mutableStateOf<Comanda?>(null) }
        var comandasList by remember { mutableStateOf<List<Comanda>>(emptyList()) }
        var isComandasLoading by remember { mutableStateOf(false) }
        var currentBookedRemark by remember { mutableStateOf(lote.bookedRemark?.trim() ?: "") }

        // REPOSITORIOS (Asume que están definidos en LoteCard)
        val comandaRepository = remember { getComandaRepository(databaseUrl) }

        val dialogWidthFraction = 0.95f
        val noOkCliente = Cliente(cliNombre = "NO OK")
        val errorColor = MaterialTheme.colorScheme.error
        val cardShape = RoundedCornerShape(12.dp)

        // Banderas de estado
        val isLoteReservedByRealClient = lote.booked != null && lote.booked.cliNombre != "NO OK"
        val isNoOkSelected = selectedCliente?.cliNombre == "NO OK"
        val isLoteReservedOrBlocked = lote.booked != null
        val isBloqueoClickable = !isLoteReservedOrBlocked || isNoOkSelected
        val isRealClientSelected = selectedCliente != null && selectedCliente!!.cliNombre != "NO OK"

        // 1. CARGAR Y FILTRAR CLIENTES
        LaunchedEffect(Unit) {
            val allClients = clientRepository.getAllClientsOrderedByName()
            clientesList = allClients.filter { it.cliNombre != "NO OK" }
        }

        // 2. CARGAR Y FILTRAR COMANDAS (Lógica Modificada para Excluir Vendidas)
        LaunchedEffect(selectedCliente) {
            comandasList = emptyList()
            selectedComanda = null
            val cliente = selectedCliente

            // Solo cargar comandas si es un cliente real
            if (cliente != null && cliente.cliNombre != "NO OK") {
                isComandasLoading = true
                try {
                    val allComandas = comandaRepository.getPendingComandasByClient(cliente.cliNombre)

                    // 🔑 PASO 1: Excluir Comandas que ya fueron vendidas.
                    val activeComandas = allComandas.filter { !it.fueVendidoComanda }

                    // 🔑 PASO 2: Encontrar la comanda previamente asignada (si aún está activa)
                    val previouslyAssignedComanda = activeComandas.firstOrNull { it.numberLoteComanda == lote.number }

                    // 🔑 PASO 3: Filtrar solo las comandas que están disponibles para este lote
                    val availableComandas = activeComandas.filter { comanda ->
                        val isUnassigned = comanda.numberLoteComanda.isNullOrBlank() || comanda.numberLoteComanda == "0"
                        val isAssignedToThisLote = comanda.numberLoteComanda == lote.number
                        isUnassigned || isAssignedToThisLote
                    }

                    comandasList = availableComandas

                    // 🔑 PASO 4: Pre-seleccionar la comanda si la reserva del lote es del cliente actual y tiene una comanda activa asociada.
                    if (lote.booked?.cliNombre == cliente.cliNombre) {
                        selectedComanda = previouslyAssignedComanda
                    }

                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("Error al cargar comandas: ${e.message}") }
                } finally {
                    isComandasLoading = false
                }
            }
        }

        // 3. DIÁLOGO PRINCIPAL (AlertDialog)
        AlertDialog(
            onDismissRequest = { showReservedDialog = false },
            modifier = Modifier.fillMaxWidth(dialogWidthFraction).fillMaxHeight(0.9f),
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reserva del lote", fontWeight = FontWeight.Bold, color = PrimaryColor)
                        Text(lote.number, fontWeight = FontWeight.Bold, color = PrimaryColor)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    lote.bookedByUser?.takeIf { it.isNotBlank() }?.let {
                        InfoCard(label = "Reservado por", value = it)
                    }

                    // --- 1. CLIENTE ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedCliente != null) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = cardShape
                            )
                            .clip(cardShape)
                            // Deshabilitamos el selector si ya hay reserva/bloqueo
                            .clickable(enabled = !isLoteReservedOrBlocked) { showClientesDialog = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = selectedCliente?.cliNombre ?: "Seleccione cliente",
                            color = if (selectedCliente != null && !isLoteReservedOrBlocked) PrimaryColor
                            else if (isLoteReservedOrBlocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    // --- 2. SECCIÓN DE COMANDAS PENDIENTES ---
                    Text("Comandas activas (${selectedCliente?.cliNombre ?: "No Seleccionado"})", fontWeight = FontWeight.Bold)

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
                        .padding(4.dp)) {

                        if (isComandasLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(30.dp), color = PrimaryColor)
                        } else if (isRealClientSelected && comandasList.isEmpty()) {
                            Text("No hay comandas pendientes para este cliente disponibles para este lote.", modifier = Modifier.align(Alignment.Center).padding(16.dp), color = TextSecondary)
                        } else if (!isRealClientSelected) {
                            Text("Lista de comandas del cliente.", modifier = Modifier.align(Alignment.Center).padding(16.dp), color = TextSecondary)
                        } else { // Mostrar la lista si hay cliente real y comandas
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(comandasList) { comanda ->

                                    // 🔑 LÓGICA DE INTERACTIVIDAD (CORREGIDA):
                                    // Es clickeable si NO hay cliente real reservando,
                                    // O si el cliente seleccionado actualmente es el que tiene la reserva del lote.
                                    // Esto permite reasignar una nueva comanda al mismo cliente después de una venta.
                                    val isComandaClickable = !isLoteReservedByRealClient || (selectedCliente?.cliNombre == lote.booked?.cliNombre)

                                    ComandaLoteCard(
                                        comanda = comanda,
                                        isSelected = comanda.idComanda == selectedComanda?.idComanda,
                                        onClick = if (isComandaClickable) { clickedComanda ->
                                            selectedComanda = if (selectedComanda?.idComanda == clickedComanda.idComanda) null else clickedComanda
                                        } else { _: Comanda -> }
                                    )
                                }
                            }
                        }
                    }
                    // ---------------------------------------------------

                    // --- 3. OBSERVACIONES ---
                    OutlinedTextField(
                        value = currentBookedRemark,
                        onValueChange = { currentBookedRemark = it },
                        label = { Text("Observaciones de reserva") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 150.dp),
                        singleLine = false,
                        shape = cardShape,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    // --- 4. BLOQUEO INTERNO (Botón) ---
                    Spacer(modifier = Modifier.height(8.dp))

                    val bloqueoBackgroundColor = when {
                        isNoOkSelected -> errorColor
                        isLoteReservedOrBlocked && !isNoOkSelected -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        isBloqueoClickable -> errorColor.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    }

                    val bloqueoIconColor = when {
                        isNoOkSelected -> Color.LightGray
                        isLoteReservedOrBlocked && !isNoOkSelected -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isBloqueoClickable -> errorColor
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                    val bloqueoTextColor = bloqueoIconColor


                    Surface(
                        shape = cardShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(cardShape)
                            .background(bloqueoBackgroundColor)
                            .then(
                                if (isBloqueoClickable) Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedCliente = noOkCliente
                                    selectedComanda = null
                                }
                                else Modifier
                            ),
                        shadowElevation = if (isBloqueoClickable) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Bloquear",
                                tint = bloqueoIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "BLOQUEO INTERNO",
                                color = bloqueoTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },
            // --- 5. Botones de Acción (ConfirmButton) ---
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Anular
                    if (lote.booked != null || lote.dateBooked != null) {
                        TextButton(onClick = {
                            showReservedDialog = false
                            scope.launch {
                                var success = true

                                // Buscamos la comanda ACTIVA vinculada a ESTE LOTE (si existe)
                                val linkedComanda = comandasList.firstOrNull { it.numberLoteComanda == lote.number }

                                if (linkedComanda != null) {
                                    val comandaCleanSuccess = comandaRepository.updateComandaLoteNumber(linkedComanda.idComanda, "")
                                    if (!comandaCleanSuccess) {
                                        snackbarHostState.showSnackbar("Error al limpiar la Comanda asociada.")
                                        success = false
                                    }
                                }

                                val loteCleanSuccess = loteRepository.updateLoteBooked(lote.id, null, null, null, null)
                                if (!loteCleanSuccess) {
                                    snackbarHostState.showSnackbar("Error al anular la reserva del Lote.")
                                    success = false
                                }

                                if (success) {
                                    onRemarkUpdated(lote.copy(booked = null, dateBooked = null, bookedByUser = null, bookedRemark = null))
                                }
                                snackbarHostState.showSnackbar(if (success) "Reserva anulada" else "Error al anular la reserva")
                            }
                        }) { Text("Anular", color = MaterialTheme.colorScheme.error) }
                    } else Spacer(modifier = Modifier.width(1.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showReservedDialog = false }) { Text("Cancelar", color = PrimaryColor) }

                        // Botón Guardar (Lógica Modificada)
                        TextButton(
                            onClick = {
                                // Validaciones
                                if (selectedCliente == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Debe seleccionar un cliente") }
                                    return@TextButton
                                }
                                if (isLoteReservedOrBlocked && selectedCliente?.cliNombre != lote.booked?.cliNombre) {
                                    scope.launch { snackbarHostState.showSnackbar("Debe anular la reserva/bloqueo existente antes de seleccionar otro cliente.") }
                                    return@TextButton
                                }

                                // Lógica de la fecha de reserva (usa fecha de Comanda si existe, si no, usa la actual o la existente)
                                val newDateBooked = if (selectedComanda != null) {
                                    selectedComanda!!.dateBookedComanda
                                } else if (lote.booked == null) {
                                    kotlinx.datetime.Clock.System.now() // Nueva reserva, usa fecha actual
                                } else {
                                    lote.dateBooked // Mantiene la fecha de la reserva existente
                                }

                                showReservedDialog = false
                                val remarkToSave = currentBookedRemark.trim().ifBlank { null }

                                scope.launch {
                                    var comandaSuccess = true

                                    // Paso 1: Desvincular la comanda anterior (si el usuario cambió la selección o deseleccionó)
                                    // Usamos 'comandasList' que contiene las comandas activas del cliente actual, excluyendo la que se selecciona ahora.
                                    val oldLinkedComanda = comandasList.firstOrNull { it.numberLoteComanda == lote.number && it.idComanda != selectedComanda?.idComanda }

                                    if (oldLinkedComanda != null) {
                                        comandaSuccess = comandaRepository.updateComandaLoteNumber(oldLinkedComanda.idComanda, "")
                                        if (!comandaSuccess) {
                                            snackbarHostState.showSnackbar("Error al desvincular la Comanda anterior.")
                                        }
                                    }

                                    // Paso 2: Actualizar la reserva del lote
                                    val loteSuccess = loteRepository.updateLoteBooked(
                                        lote.id, selectedCliente, newDateBooked, userToSave, remarkToSave
                                    )

                                    // Paso 3: Vincular la nueva comanda (si se seleccionó una y la reserva del lote fue exitosa)
                                    if (loteSuccess && selectedComanda != null && comandaSuccess) {
                                        comandaSuccess = comandaRepository.updateComandaLoteNumber(
                                            selectedComanda!!.idComanda, lote.number
                                        )
                                    }

                                    if (loteSuccess && comandaSuccess) {
                                        val updatedLote = lote.copy(
                                            booked = selectedCliente,
                                            dateBooked = newDateBooked,
                                            bookedByUser = userToSave,
                                            bookedRemark = remarkToSave
                                        )
                                        onRemarkUpdated(updatedLote)

                                        val msg = if (selectedComanda != null) "Reserva y Comanda asociadas correctamente"
                                        else "Reserva del lote guardada (sin Comanda asociada)"
                                        snackbarHostState.showSnackbar(msg)
                                    } else {
                                        snackbarHostState.showSnackbar("Error al guardar la reserva. Lote: $loteSuccess, Comanda: $comandaSuccess")
                                    }
                                }
                            },
                            // Habilitación: Solo requiere que se haya seleccionado un cliente
                            enabled = selectedCliente != null
                        ) { Text("Guardar", color = PrimaryColor) }
                    }
                }
            }
        )

        // --- DIÁLOGO DE SELECCIÓN DE CLIENTE ---
        if (showClientesDialog) {
            var tempCliente by remember { mutableStateOf(selectedCliente) }

            AlertDialog(
                onDismissRequest = { showClientesDialog = false },
                title = { Text("Seleccione un cliente", fontWeight = FontWeight.Bold, color = PrimaryColor) },
                text = {
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        LazyColumn {
                            items(clientesList ?: emptyList()) { cliente ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tempCliente = cliente }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = tempCliente == cliente,
                                        onClick = { tempCliente = cliente },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(cliente.cliNombre)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedCliente = tempCliente
                        showClientesDialog = false
                    }) {
                        Text("Aceptar", color = PrimaryColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClientesDialog = false }) {
                        Text("Cancelar", color = PrimaryColor)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

    }

}

