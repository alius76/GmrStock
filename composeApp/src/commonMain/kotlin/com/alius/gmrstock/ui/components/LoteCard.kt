package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoteCard(
    lote: LoteModel,
    certificado: Certificado?,
    certificadoIconColor: Color,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onViewBigBags: (List<BigBags>) -> Unit,
    databaseUrl: String
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showReservedDialog by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }

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
            // --- Header: Lote + Certificado + Observación + Ver BigBags ---
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
                    // Ícono certificado
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

                    // Ícono Observación
                    if (lote.remark.isNotBlank()) {
                        IconButton(
                            onClick = { showRemarkDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = "Ver observación",
                                tint = PrimaryColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Ícono Ver BigBags
                    IconButton(
                        onClick = { showBigBagsDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ViewList,
                            contentDescription = "Ver BigBags",
                            tint = PrimaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Detalles ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Material", lote.description)
                DetailRow("Fecha", formatInstant(lote.date))
                DetailRow("Ubicación", lote.location)
                DetailRow("BigBags", lote.count.toString())

                // Nuevo Row para el peso y el chip de reservado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Texto del peso total
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Peso total",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${lote.totalWeight} Kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryColor
                        )
                    }

                    // Chip de reservado personalizado
                    if (lote.booked != null && lote.booked.cliNombre.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(ReservedColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { showReservedDialog = true }
                                .padding(horizontal = 8.dp, vertical = 2.dp), // Ajustamos el padding para reducir altura
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ReservedColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reservado",
                                    fontWeight = FontWeight.SemiBold,
                                    color = ReservedColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Diálogo Observación ---
    if (showRemarkDialog) {
        AlertDialog(
            onDismissRequest = { showRemarkDialog = false },
            title = { Text("Observación", fontWeight = FontWeight.Bold, color = PrimaryColor) },
            text = { Text(lote.remark) },
            confirmButton = {
                TextButton(onClick = { showRemarkDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
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

                    // Iteramos sobre la lista de 'parametros'
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Aumentamos el espacio entre parámetros
                    ) {
                        certificado.parametros.forEach { parametro ->
                            // Contenedor principal para cada parámetro
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Fila 1: Descripción y valor (con advertencia)
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

                                // Fila 2: Rango (debajo de la descripción)
                                val rangoTexto = parametro.rango?.let { rango ->
                                    if (rango.valorMin != null && rango.valorMax != null) {
                                        "Rango: (${rango.valorMin} - ${rango.valorMax} ${parametro.unidad})"
                                    } else {
                                        "Rango: N/A"
                                    }
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