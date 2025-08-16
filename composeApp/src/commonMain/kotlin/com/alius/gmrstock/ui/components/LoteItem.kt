package com.alius.gmrstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.BadgeTextColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.alius.gmrstock.core.utils.formatInstant

@Composable
fun LoteItem(lote: LoteModel) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showBigBagsDialog = true }, // 👈 ahora abre el diálogo
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Texto Lote
                Text(
                    text = "Lote: ${lote.number}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    maxLines = 1
                )

                // Badge de cantidad
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

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 50.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Lote",
                    tint = PrimaryColor,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Material: ${lote.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Fecha: ${formatInstant(lote.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ubicación: ${lote.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Peso total: ${lote.totalWeight} Kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }

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
