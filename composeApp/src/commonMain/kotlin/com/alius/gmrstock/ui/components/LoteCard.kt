package com.alius.gmrstock.ui.components


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
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
    // -----------------------------------------------------------
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showReservedDialog by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showAddRemarkDialog by remember { mutableStateOf(false) }

    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0

    // Estado para la observación general (no de reserva)
    var currentRemarkText by remember { mutableStateOf(lote.remark) }

    // ⬇️ ESTADO PARA LA OBSERVACIÓN DE RESERVA ⬇️
    var currentBookedRemark by remember { mutableStateOf(lote.bookedRemark ?: "") }
    // ------------------------------------------

    val loteRepository = remember { getLoteRepository(databaseUrl) }

    val hasRemark = lote.remark.isNotBlank()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
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

            // 1. CABECERA REESTRUCTURADA: Lote Grande + Botones Debajo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Número del lote más grande y centrado
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Botones debajo, organizados en una fila
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reservado
                    IconButton(
                        onClick = {
                            currentBookedRemark = lote.bookedRemark ?: "" // ⬅️ Inicializamos el estado antes de abrir
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
                            tint = if (hasRemark) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Certificado
                    IconButton(
                        onClick = {
                            if (certificado != null) {
                                showCertificadoDialog = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No se encontró certificado para el lote ${lote.number}")
                                }
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
            } // Fin de Column(Cabecera)

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

                // Etiqueta reservado en la esquina inferior derecha en dos líneas
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

    // --- Función auxiliar para mostrar información no editable ---
    @Composable
    fun InfoCard(label: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
    // -------------------------------------------------------------

    // --- Diálogos Observación General ---
    if (showRemarkDialog) {
        val isChanged = currentRemarkText.trim() != lote.remark.trim()

        AlertDialog(
            onDismissRequest = { showRemarkDialog = false },
            title = {
                // ✅ MODIFICACIÓN: Contenedor para centrar el título en el Dialog
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center // Alinea el contenido (Column) al centro
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Observación del Lote",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = lote.number, // El número de lote en una segunda línea
                            fontWeight = FontWeight.ExtraBold, // Opcional: hacerlo más destacado
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            text = {
                OutlinedTextField(
                    // ... (El resto del TextField es igual)
                    value = currentRemarkText,
                    onValueChange = { currentRemarkText = it },
                    label = { Text("Editar observación") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
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
                // ... (confirmButton sigue siendo igual)
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
                                snackbarHostState.showSnackbar(
                                    if (success) "Observación eliminada"
                                    else "Error al eliminar la observación"
                                )
                            }
                        },
                        enabled = lote.remark.isNotBlank()
                    ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }

                    Row(horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRemarkDialog = false }) {
                            Text("Cerrar", color = PrimaryColor)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                showRemarkDialog = false
                                val remarkToSave = currentRemarkText.trim()
                                scope.launch {
                                    val success =
                                        loteRepository.updateLoteRemark(lote.id, remarkToSave)
                                    if (success) onRemarkUpdated(lote.copy(remark = remarkToSave))
                                    snackbarHostState.showSnackbar(
                                        if (success) "Observación actualizada"
                                        else "Error al actualizar la observación"
                                    )
                                }
                            },
                            enabled = isChanged && currentRemarkText.isNotBlank()
                        ) { Text("Guardar", color = PrimaryColor) }
                    }
                }
            }
        )
    }

// --- Diálogo Añadir Observación (Modificado) ---
    if (showAddRemarkDialog) {
        AlertDialog(
            onDismissRequest = { showAddRemarkDialog = false },
            title = {
                // ✅ MODIFICACIÓN: Contenedor para centrar el título en el Dialog
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center // Alinea el contenido (Column) al centro
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Observación del Lote",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = lote.number,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            text = {
                OutlinedTextField(
                    // ... (El resto del TextField es igual)
                    value = currentRemarkText,
                    onValueChange = { currentRemarkText = it },
                    label = { Text("Escribe tu observación") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
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
                            snackbarHostState.showSnackbar(
                                if (success) "Observación guardada"
                                else "Error al guardar la observación"
                            )
                        }
                    },
                    enabled = currentRemarkText.isNotBlank()
                ) { Text("Guardar", color = PrimaryColor) }
            },
            dismissButton = {
                TextButton(onClick = { showAddRemarkDialog = false }) {
                    Text(
                        "Cancelar",
                        color = PrimaryColor
                    )
                }
            }
        )
    }
    // --- Diálogo BigBags ---
    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
                }
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Lista de BigBags",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = { BigBagsDialogContent(bigBags = lote.bigBag) }
        )
    }

    // --- Diálogo Certificado (Sin cambios necesarios) ---
    if (showCertificadoDialog && certificado != null) {
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
                    val (icon, estadoText, estadoColor) = when (certificado.status) {
                        CertificadoStatus.ADVERTENCIA -> Triple(
                            Icons.Default.Warning,
                            "Advertencia",
                            MaterialTheme.colorScheme.error
                        )

                        CertificadoStatus.CORRECTO -> Triple(
                            Icons.Default.CheckCircle,
                            "Correcto",
                            PrimaryColor
                        )

                        else -> Triple(
                            Icons.Default.Description,
                            "Sin Datos",
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = estadoText,
                        tint = estadoColor,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Certificado de ${lote.number}",
                        color = PrimaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        certificado.parametros.forEach { parametro ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = parametro.descripcion,
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
                                            text = parametro.valor,
                                            color = if (parametro.warning) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurface,
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

                                Text(
                                    text = rangoTexto,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { showCertificadoDialog = false },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Cerrar", color = PrimaryColor)
                    }
                }
            }
        }
    }


    // DIALOG DE RESERVAS COMPLETO (versión corregida)
    if (showReservedDialog) {
        // 1. ESTADOS Y ALMACENAMIENTO DE VALORES ORIGINALES
        var selectedCliente by remember { mutableStateOf(lote.booked) }
        var fecha by remember { mutableStateOf(formatInstant(lote.dateBooked)) }
        var fechaEditable by remember { mutableStateOf(lote.booked == null) }

        // Observaciones sincronizadas
        LaunchedEffect(lote.id) {
            currentBookedRemark = lote.bookedRemark?.trim() ?: ""
        }

        val originalRemarkClean: String? = remember(lote.id) {
            val remarkFromLote = lote.bookedRemark?.trim()?.ifBlank { null }
            val remarkFromState = currentBookedRemark.trim().ifBlank { null }
            remarkFromLote ?: remarkFromState
        }

        val originalDateString = remember { formatInstant(lote.dateBooked) }
        val originalCliente = remember { lote.booked }

        var clientesList by remember { mutableStateOf<List<Cliente>>(emptyList()) }

        LaunchedEffect(Unit) {
            clientesList = clientRepository.getAllClientsOrderedByName()
        }

        // 2. DIÁLOGO
        AlertDialog(
            onDismissRequest = { showReservedDialog = false },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reserva del Lote",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = lote.number,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Mostrar reservado por solo si hay valor
                    if (!lote.bookedByUser.isNullOrBlank()) {
                        InfoCard(label = "Reservado por", value = lote.bookedByUser)
                    }

                    // CLIENTE
                    if (lote.booked != null) {
                        OutlinedTextField(
                            value = selectedCliente?.cliNombre ?: "",
                            onValueChange = {},
                            label = { Text("Cliente") },
                            readOnly = true,
                            enabled = false,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Seleccione Cliente", fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            items(clientesList) { cliente ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedCliente == cliente) PrimaryColor else MaterialTheme.colorScheme.surface,
                                    tonalElevation = if (selectedCliente == cliente) 4.dp else 0.dp,
                                    modifier = Modifier.clickable { selectedCliente = cliente }
                                ) {
                                    Text(
                                        text = cliente.cliNombre,
                                        color = if (selectedCliente == cliente)
                                            MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // OBSERVACIONES DE RESERVA
                    OutlinedTextField(
                        value = currentBookedRemark,
                        onValueChange = { currentBookedRemark = it },
                        label = { Text("Observaciones de reserva") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 150.dp),
                        singleLine = false,
                        readOnly = lote.booked != null,
                        enabled = lote.booked == null, // Igual que Cliente: si hay reserva, se deshabilita
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

// FECHA
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { input ->
                            if (fechaEditable) {
                                val filtered = input.filter { it.isDigit() || it == '-' }
                                if (filtered.length <= 10) fecha = filtered
                            }
                        },
                        label = { Text(
                            "Fecha estimada (DD-MM-YYYY)",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        },
                        singleLine = true,
                        readOnly = !fechaEditable,
                        enabled = fechaEditable, // Igual que Cliente: deshabilitada si no editable
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        trailingIcon = {
                            if (fecha.isNotBlank() || !fechaEditable) {
                                IconButton(onClick = { fecha = ""; fechaEditable = true }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Borrar fecha")
                                }
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )
                }
            },
            confirmButton = {
                val isDateChanged = fecha.trim() != originalDateString.trim()
                val isClientChanged = originalCliente != selectedCliente
                val currentRemarkClean = currentBookedRemark.trim().ifBlank { null }
                val isRemarkChanged = currentRemarkClean != originalRemarkClean
                val isSaveEnabled = (selectedCliente != null || lote.booked != null) &&
                        (isDateChanged || isRemarkChanged || isClientChanged)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Anular reserva
                    if (lote.booked != null || lote.dateBooked != null) {
                        TextButton(onClick = {
                            showReservedDialog = false
                            scope.launch {
                                val success = loteRepository.updateLoteBooked(
                                    loteId = lote.id,
                                    cliente = null,
                                    dateBooked = null,
                                    bookedByUser = null,
                                    bookedRemark = null
                                )
                                if (success) onRemarkUpdated(
                                    lote.copy(booked = null, dateBooked = null, bookedByUser = null, bookedRemark = null)
                                )
                                snackbarHostState.showSnackbar(
                                    if (success) "Reserva anulada" else "Error al anular la reserva"
                                )
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
                                        val parts = fecha.trim().split("-")
                                        if (parts.size == 3) {
                                            val day = parts[0].toInt()
                                            val month = parts[1].toInt()
                                            val year = parts[2].toInt()
                                            if (day in 1..31 && month in 1..12 && year >= 1900) {
                                                kotlinx.datetime.LocalDate(year, month, day)
                                                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                                            } else null
                                        } else null
                                    } else null
                                } catch (e: Exception) { null }

                                if (fecha.isNotBlank() && parsedDate == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Formato o rango de fecha inválido. Use DD-MM-YYYY") }
                                    return@TextButton
                                }

                                showReservedDialog = false

                                // Usuario a guardar
                                val userToSave = if (!lote.bookedByUser.isNullOrBlank()) lote.bookedByUser else currentUserEmail
                                val remarkToSave = currentBookedRemark.trim().ifBlank { null }

                                scope.launch {
                                    val success = loteRepository.updateLoteBooked(
                                        loteId = lote.id,
                                        cliente = selectedCliente,
                                        dateBooked = parsedDate,
                                        bookedByUser = userToSave,
                                        bookedRemark = remarkToSave
                                    )
                                    if (success) {
                                        val updatedLote = lote.copy(
                                            booked = selectedCliente,
                                            dateBooked = parsedDate,
                                            bookedByUser = userToSave,
                                            bookedRemark = remarkToSave
                                        )
                                        onRemarkUpdated(updatedLote)
                                        snackbarHostState.showSnackbar("Reserva guardada correctamente")
                                    } else {
                                        snackbarHostState.showSnackbar("Error al guardar la reserva")
                                    }
                                }
                            },
                            enabled = isSaveEnabled
                        ) { Text("Guardar", color = PrimaryColor) }
                    }
                }
            }
        )
    }


}