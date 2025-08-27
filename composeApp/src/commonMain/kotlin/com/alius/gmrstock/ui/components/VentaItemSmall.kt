package com.alius.gmrstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.ui.theme.BadgeTextColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary
import com.alius.gmrstock.ui.theme.TextSecondary

@Composable
fun VentaItemSmall(venta: Venta, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(200.dp)
            .height(220.dp)
            .padding(6.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            // ✅ Usa Arrangement.Top para alinear el contenido arriba
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bloque 1: Nombre del cliente
            Text(
                text = venta.ventaCliente,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                fontSize = 14.sp,
                maxLines = 1
            )
            // ✅ Spacer para agregar margen entre el texto y el ícono
            Spacer(modifier = Modifier.height(2.dp))

            // 🔹 Ícono de Euro
            Icon(
                imageVector = Icons.Filled.EuroSymbol,
                contentDescription = "Euro Icon",
                tint = PrimaryColor,
                modifier = Modifier.size(32.dp)
            )

            // ✅ Spacer para separar el ícono del siguiente bloque
            Spacer(modifier = Modifier.height(12.dp))

            // Bloque 2: Lote, material y fecha
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lote: ${venta.ventaLote}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = venta.ventaMaterial,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = formatInstant(venta.ventaFecha),
                    color = TextPrimary
                )
            }

            // ✅ Spacer con peso para empujar el badge hacia abajo
            Spacer(modifier = Modifier.weight(1f))

            // Bloque 3: Badge al final centrado
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