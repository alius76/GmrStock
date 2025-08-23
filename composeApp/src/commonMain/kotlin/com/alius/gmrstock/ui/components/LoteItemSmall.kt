package com.alius.gmrstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary
import com.alius.gmrstock.ui.theme.BadgeTextColor

@Composable
fun LoteItemSmall(lote: LoteModel) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(220.dp)
            .padding(6.dp)
            .clickable { showBigBagsDialog = true },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bloque 1: Número del lote
            Text(
                text = "Lote: ${lote.number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                maxLines = 1
            )

            // NUEVO: Icono debajo del número de lote
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = "Lote",
                tint = PrimaryColor,
                modifier = Modifier.size(36.dp)
            )

            // Bloque 2: Descripción, fecha, ubicación y peso
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = lote.description,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Fecha: ${formatInstant(lote.date)}",
                    color = TextPrimary
                )
                Text(
                    text = lote.location,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "Peso: ${lote.totalWeight} Kg",
                    color = TextPrimary
                )
            }

            // Bloque 3: Cantidad en badge al final
            Box(
                modifier = Modifier
                    .background(SecondaryColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BigBags ${lote.count}",
                    fontWeight = FontWeight.SemiBold,
                    color = BadgeTextColor
                )
            }
        }
    }

    // Diálogo de BigBags
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
                    text = "Lista de BigBags",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                BigBagsDialogContent(bigBags = lote.bigBag)
            }
        )
    }
}
