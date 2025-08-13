package com.alius.gmrstock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.domain.model.Venta
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun VentaItem(venta: Venta) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = venta.ventaCliente,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF029083)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Lote: ${venta.ventaLote}", color = Color.DarkGray)
            Text(text = "Material: ${venta.ventaMaterial}", color = Color.DarkGray)
            Text(text = "Fecha: ${formatInstant(venta.ventaFecha)}", color = Color.DarkGray)
        }
    }

    // Diálogo de BigBags
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("BigBags vendidos") },
            text = {
                VentaBigBagsDialogContent(venta.ventaBigbags)
            }
        )
    }
}

fun formatInstant(instant: Instant?): String {
    if (instant == null) return ""
    val localDate = instant.toLocalDateTime(TimeZone.UTC).date
    return "${localDate.dayOfMonth.toString().padStart(2, '0')}/" +
            "${localDate.monthNumber.toString().padStart(2, '0')}/" +
            "${localDate.year}"
}
