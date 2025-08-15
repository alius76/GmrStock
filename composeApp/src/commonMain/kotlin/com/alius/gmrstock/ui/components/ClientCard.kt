package com.alius.gmrstock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.theme.PrimaryColor

@Composable
fun ClientCard(
    cliente: Cliente,
    venta: Venta,
    modifier: Modifier = Modifier
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
                }
            },
            title = {
                Text(
                    text = "BigBags vendidos",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                VentaBigBagsDialogContent(bigBags = venta.ventaBigbags)
            }
        )
    }

    // Calcular cantidad de BigBags y peso total
    val cantidadBigBags = venta.ventaBigbags.size
    val pesoTotal = venta.ventaBigbags.sumOf { it.ventaBbWeight.toIntOrNull() ?: 0 }

    Card(
        modifier = modifier
            .width(300.dp)
            .wrapContentHeight()
            .clickable { showBigBagsDialog = true }, // abrir diálogo al hacer click
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre del lote (anteriormente estaba usando venta.ventaLote)
            Text(
                text = "Lote: ${venta.ventaLote}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PrimaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Datos de la venta
            DetailRow("Material:", venta.ventaMaterial ?: "Sin material")
            DetailRow("Fecha:", formatInstant(venta.ventaFecha))
            DetailRow("BigBags:", cantidadBigBags.toString())
            DetailRow("Peso total:", "${pesoTotal.toString()} Kg", PrimaryColor)

        }
    }
}


@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
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
