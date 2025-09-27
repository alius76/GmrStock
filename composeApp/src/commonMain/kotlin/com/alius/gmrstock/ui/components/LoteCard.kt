package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.alius.gmrstock.ui.components.BigBagsDialogContent
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.ReservedColor
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.data.getLoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    onRemarkUpdated: (LoteModel) -> Unit
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showReservedDialog by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showAddRemarkDialog by remember { mutableStateOf(false) }

    // Siempre usamos el valor actual del lote
    var currentRemarkText by remember { mutableStateOf(lote.remark) }

    val loteRepository = remember { getLoteRepository(databaseUrl) }

    // ❌ NO usamos remember(lote), solo evaluamos el valor actual
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (lote.booked != null && lote.booked.cliNombre.isNotBlank()) {
                        IconButton(
                            onClick = { showReservedDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Ver reservado",
                                tint = ReservedColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            currentRemarkText = lote.remark
                            if (hasRemark) showRemarkDialog = true
                            else showAddRemarkDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (hasRemark) Icons.Default.Description else Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = if (hasRemark) "Ver/Editar observación" else "Añadir observación",
                            tint = if (hasRemark) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Material", lote.description)
                DetailRow("Fecha", formatInstant(lote.date))
                DetailRow("Ubicación", lote.location)
                DetailRow("BigBags", lote.count.toString())
                DetailRow("Peso total", "${lote.totalWeight} Kg", PrimaryColor)
            }
        }
    }

    // --- Diálogos Observación ---
    if (showRemarkDialog) {
        val isChanged = currentRemarkText.trim() != lote.remark.trim()

        AlertDialog(
            onDismissRequest = { showRemarkDialog = false },
            title = { Text("Observación del Lote ${lote.number}", fontWeight = FontWeight.Bold, color = PrimaryColor) },
            text = {
                OutlinedTextField(
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
                                    val success = loteRepository.updateLoteRemark(lote.id, remarkToSave)
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

    if (showAddRemarkDialog) {
        AlertDialog(
            onDismissRequest = { showAddRemarkDialog = false },
            title = { Text("Añadir observación", fontWeight = FontWeight.Bold, color = PrimaryColor) },
            text = {
                OutlinedTextField(
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
                TextButton(onClick = { showAddRemarkDialog = false }) { Text("Cancelar", color = PrimaryColor) }
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

    // --- Diálogo Certificado ---
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
                        CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
                        CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
                        else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        "Rango: (${rango.valorMin} - ${rango.valorMax} ${parametro.unidad})"
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

    // --- Diálogo Reservado ---
    if (showReservedDialog) {
        AlertDialog(
            onDismissRequest = { showReservedDialog = false },
            title = { Text("Reservado", fontWeight = FontWeight.Bold, color = PrimaryColor) },
            text = {
                Column {
                    Text("Nombre: ${lote.booked?.cliNombre}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Observaciones: ${lote.booked?.cliObservaciones ?: "-"}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showReservedDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
                }
            }
        )
    }
}
