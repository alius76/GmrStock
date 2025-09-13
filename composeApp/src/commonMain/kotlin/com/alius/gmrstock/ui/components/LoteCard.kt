import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.LoteModel
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { showBigBagsDialog = true },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lote.number,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lote.booked != null && lote.booked.cliNombre.isNotBlank()) {
                        BookedTooltipIcon(lote = lote)
                        Spacer(modifier = Modifier.width(8.dp))
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Ver certificado",
                            tint = certificadoIconColor
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            DetailRow("Material:", lote.description)
            DetailRow("Fecha:", formatInstant(lote.date))
            DetailRow("Ubicación:", lote.location)
            DetailRow("BigBags:", lote.count)
            DetailRow("Peso total:", "${lote.totalWeight} Kg", PrimaryColor)
            DetailRow("Observación:", lote.remark)
        }
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
            title = { Text("Lista de BigBags", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Icono de estado grande ---
                    val (icon, estadoText, estadoColor) = when (certificado.status) {
                        "w" -> Triple(
                            Icons.Default.Warning,
                            "Advertencia",
                            MaterialTheme.colorScheme.error
                        )
                        "c" -> Triple(
                            Icons.Default.CheckCircle,
                            "Correcto",
                            PrimaryColor
                        )
                        else -> Triple(
                            Icons.Default.Description,
                            "Desconocido",
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = estadoText,
                        tint = estadoColor,
                        modifier = Modifier.size(48.dp)
                    )

                    // --- Título ---
                    Text(
                        text = "Certificado de ${lote.number}",
                        color = PrimaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    // --- Estado ---
                  //  Text(
                  //      text = "Estado: $estadoText",
                  //      color = estadoColor,
                  //      fontWeight = FontWeight.Bold,
                  //      fontSize = 18.sp,
                  //      textAlign = TextAlign.Center
                  //  )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // --- Encabezados ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Propiedades",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Valores",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // --- Lista propiedades ---
                    certificado.propiedades.forEach { prop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = prop.nombre,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, // ⬅️ Solo una línea
                                overflow = TextOverflow.Ellipsis, // ⬅️ Si es muy largo, muestra "..."
                                modifier = Modifier
                                    .widthIn(max = 220.dp) // ⬅️ Limita ancho máximo
                                    .padding(end = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (prop.warning) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Advertencia",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = prop.valor,
                                    color = if (prop.warning) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Botón cerrar ---
                    Button(
                        onClick = { showCertificadoDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------
// BookedTooltipIcon
// -------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookedTooltipIcon(lote: LoteModel) {
    var showTooltip by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Default.VpnKey,
            contentDescription = "Lote reservado",
            tint = ReservedColor,
            modifier = Modifier
                .size(24.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showTooltip = true }
                )
        )

        DropdownMenu(
            expanded = showTooltip,
            onDismissRequest = { showTooltip = false },
            offset = DpOffset(x = 0.dp, y = 8.dp)
        ) {
            Text(
                text = "Nombre: ${lote.booked?.cliNombre}",
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "Observaciones: ${lote.booked?.cliObservaciones ?: "-"}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// -------------------------
// DetailRow
// -------------------------
@Composable
fun DetailRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 100.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
