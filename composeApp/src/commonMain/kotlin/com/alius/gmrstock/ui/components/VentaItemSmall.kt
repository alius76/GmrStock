package com.alius.gmrstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.core.utils.formatInstant

@Composable
fun VentaItemSmall(venta: Venta, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(160.dp)
            .padding(4.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Nombre del cliente + Badge a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = venta.ventaCliente,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF029083),
                    maxLines = 1
                )

                Box(
                    modifier = Modifier
                        .background(Color(0xFFE0F7F4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${venta.ventaBigbags.size} BigBags",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF029083)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Lote: ${venta.ventaLote}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            Text(text = venta.ventaMaterial, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatInstant(venta.ventaFecha),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }

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
