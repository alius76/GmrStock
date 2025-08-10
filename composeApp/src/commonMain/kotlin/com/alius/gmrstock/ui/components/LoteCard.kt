import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.components.BigBagsDialogContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.*

@Composable
fun LoteCard(
    lote: LoteModel,
    modifier: Modifier = Modifier,
    customGreenColor: androidx.compose.ui.graphics.Color,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onGeneratePdf: suspend (LoteModel) -> Unit,
    onViewBigBags: (List<BigBags>) -> Unit
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    fun formatInstantToDDMMYYYY(instant: Instant?): String {
        if (instant == null) return "N/A"
        return try {
            // Convertir a zona horaria local
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year.toString()
            "$day/$month/$year"
        } catch (e: Exception) {
            "N/A"
        }
    }


    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
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
                    text = "Lote: ${lote.number}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = customGreenColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            try {
                                onGeneratePdf(lote)
                                snackbarHostState.showSnackbar("PDF generado correctamente para el lote ${lote.number}")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al generar PDF: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Generar PDF",
                        tint = customGreenColor
                    )
                }

                IconButton(
                    onClick = { showBigBagsDialog = true }
                ) {
                    Icon(
                        Icons.Default.Expand,
                        contentDescription = "Ver Bigbags",
                        tint = customGreenColor
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            DetailRow("Descripción:", lote.description)
            DetailRow("Fecha:", formatInstantToDDMMYYYY(lote.date))
            DetailRow("Ubicación:", lote.location)
            DetailRow("Cantidad:", lote.count)
            DetailRow("Observación:", lote.remark)
            DetailRow("Peso total:", "${lote.totalWeight} Kg", customGreenColor)
        }
    }

    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) {
                    Text(
                        "Cerrar",
                        color = customGreenColor
                    )
                }
            },
            title = {
                Text(
                    text = "Lista de BigBags",
                    color = customGreenColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                BigBagsDialogContent(bigBags = lote.bigBag)
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 100.dp) // ancho mínimo para etiquetas
        )
        Spacer(modifier = Modifier.width(8.dp)) // espacio pequeño entre etiqueta y valor
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
