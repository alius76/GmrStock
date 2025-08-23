package com.alius.gmrstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.ui.theme.BadgeTextColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary

@Composable
fun VentaItem(venta: Venta, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila superior: Nombre cliente + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = venta.ventaCliente,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                Box(
                    modifier = Modifier
                        .background(SecondaryColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BigBags ${venta.ventaBigbags.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BadgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de información del lote con icono
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.EuroSymbol,
                    contentDescription = "Lote",
                    tint = PrimaryColor,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = "Lote: ${venta.ventaLote}", color = TextPrimary)
                    Text(text = "Material: ${venta.ventaMaterial}", color = TextPrimary)
                    Text(text = "Fecha: ${formatInstant(venta.ventaFecha)}", color = TextPrimary)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
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
                VentaBigBagsDialogContent(venta.ventaBigbags)
            }
        )
    }
}
