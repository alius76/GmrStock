package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

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
                        onClick = {
                            if (certificado != null) showCertificadoDialog = true
                            else scope.launch {
                                snackbarHostState.showSnackbar("No se encontró certificado para el lote ${lote.number}")
                            }
                        },
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
    if (showCertificadoDialog && certificado != null) {
        Dialog(onDismissRequest = { showCertificadoDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, estadoText, estadoColor) = when (certificado.status) {
                        CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
                        CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
                        else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Icon(icon, contentDescription = estadoText, tint = estadoColor, modifier = Modifier.size(48.dp))
                    Text("Certificado de ${lote.number}", color = PrimaryColor, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, textAlign = TextAlign.Center)
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        certificado.parametros.forEach { parametro ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(parametro.descripcion, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
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
        var fecha by remember { mutableStateOf(formatInstant(lote.dateBooked)) }
        var showDatePicker by remember { mutableStateOf(false) }
        var showClientesDialog by remember { mutableStateOf(false) }
        var userToSave by remember { mutableStateOf(currentUserEmail) }
        var clientesList by remember { mutableStateOf<List<Cliente>?>(null) }

        val dialogWidthFraction = 0.9f

        LaunchedEffect(lote.id) { currentBookedRemark = lote.bookedRemark?.trim() ?: "" }

        LaunchedEffect(Unit) {
            val allClients = clientRepository.getAllClientsOrderedByName()
            clientesList = allClients.filter { it.cliNombre != "NO OK" }
        }

        AlertDialog(
            onDismissRequest = { showReservedDialog = false },
            modifier = Modifier.fillMaxWidth(dialogWidthFraction),
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reserva del Lote", fontWeight = FontWeight.Bold, color = PrimaryColor)
                        Text(lote.number, fontWeight = FontWeight.Bold, color = PrimaryColor)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    lote.bookedByUser?.takeIf { it.isNotBlank() }?.let {
                        InfoCard(label = "Reservado por", value = it)
                    }

                    // --- CLIENTE ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedCliente != null) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { showClientesDialog = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = selectedCliente?.cliNombre ?: "Reservado al cliente",
                            color = if (selectedCliente != null) PrimaryColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    // Bloqueo "NO OK"
                    Spacer(modifier = Modifier.height(8.dp))
                    val noOkCliente = Cliente(cliNombre = "NO OK")
                    val isNoOkSelected = selectedCliente?.cliNombre == "NO OK"
                    val errorColor = MaterialTheme.colorScheme.error

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNoOkSelected) errorColor else errorColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable { selectedCliente = noOkCliente }
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
                                tint = if (isNoOkSelected) Color.White else errorColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "BLOQUEO INTERNO",
                                color = if (isNoOkSelected) Color.White else errorColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // --- OBSERVACIONES ---
                    OutlinedTextField(
                        value = currentBookedRemark,
                        onValueChange = { currentBookedRemark = it },
                        label = { Text("Observaciones de reserva") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 150.dp),
                        singleLine = false,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    // --- FECHA ---
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fecha estimada", fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Calendario",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (fecha.isNotBlank()) fecha else "Seleccione fecha",
                                    color = if (fecha.isNotBlank()) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (fecha.isNotBlank()) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Borrar fecha",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp).clickable { fecha = "" }
                                )
                            }
                        }
                    }

                    if (showDatePicker) {
                        UniversalDatePickerDialog(
                            initialDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                            onDateSelected = { selected ->
                                fecha = "${selected.dayOfMonth.toString().padStart(2,'0')}-${selected.monthNumber.toString().padStart(2,'0')}-${selected.year}"
                                showDatePicker = false
                            },
                            onDismiss = { showDatePicker = false },
                            primaryColor = PrimaryColor
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (lote.booked != null || lote.dateBooked != null) {
                        TextButton(onClick = {
                            showReservedDialog = false
                            scope.launch {
                                val success = loteRepository.updateLoteBooked(lote.id, null, null, null, null)
                                if (success) onRemarkUpdated(lote.copy(booked = null, dateBooked = null, bookedByUser = null, bookedRemark = null))
                                snackbarHostState.showSnackbar(if (success) "Reserva anulada" else "Error al anular la reserva")
                            }
                        }) { Text("Anular", color = MaterialTheme.colorScheme.error) }
                    } else Spacer(modifier = Modifier.width(1.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showReservedDialog = false }) { Text("Cancelar", color = PrimaryColor) }
                        TextButton(
                            onClick = {
                                if (selectedCliente == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Debe seleccionar un cliente") }
                                    return@TextButton
                                }

                                val parsedDate = try {
                                    if (fecha.isNotBlank()) {
                                        val parts = fecha.split("-")
                                        if (parts.size == 3) {
                                            val day = parts[0].toInt()
                                            val month = parts[1].toInt()
                                            val year = parts[2].toInt()
                                            kotlinx.datetime.LocalDate(year, month, day)
                                                .atStartOfDayIn(TimeZone.currentSystemDefault())
                                        } else null
                                    } else null
                                } catch (e: Exception) { null }

                                showReservedDialog = false
                                val remarkToSave = currentBookedRemark.trim().ifBlank { null }

                                scope.launch {
                                    val success = loteRepository.updateLoteBooked(
                                        lote.id, selectedCliente, parsedDate ?: lote.dateBooked, userToSave, remarkToSave
                                    )
                                    if (success) {
                                        val updatedLote = lote.copy(
                                            booked = selectedCliente,
                                            dateBooked = parsedDate ?: lote.dateBooked,
                                            bookedByUser = userToSave,
                                            bookedRemark = remarkToSave
                                        )
                                        onRemarkUpdated(updatedLote)
                                        snackbarHostState.showSnackbar("Reserva guardada correctamente")
                                    } else snackbarHostState.showSnackbar("Error al guardar la reserva")
                                }
                            },
                            enabled = selectedCliente != null
                        ) { Text("Guardar", color = PrimaryColor) }
                    }
                }
            }
        )

        // --- DIÁLOGO DE SELECCIÓN DE CLIENTE ---
        if (showClientesDialog) {
            Dialog(onDismissRequest = { showClientesDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(dialogWidthFraction)
                        .heightIn(max = 500.dp), // altura máxima
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
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

                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scrollState)
                        ) {
                            ClientesSelectedDialogContent(
                                clientes = clientesList ?: emptyList(),
                                selectedCliente = selectedCliente,
                                onClienteSelected = { cliente ->
                                    selectedCliente = cliente
                                    showClientesDialog = false
                                },
                                onDismiss = { showClientesDialog = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { showClientesDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cerrar", color = PrimaryColor)
                        }
                    }
                }
            }
        }
    }


}

