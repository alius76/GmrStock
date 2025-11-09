package com.alius.gmrstock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.domain.model.Reprocesar
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary
import com.alius.gmrstock.ui.theme.TextSecondary
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.core.utils.formatWeight

@Composable
fun ReprocesarCard(reproceso: Reprocesar) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    val totalWeightNumber = reproceso.reprocesoLoteWeight.toDoubleOrNull() ?: 0.0
    val bigBagCount = reproceso.reprocesoBigBag.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { showBigBagsDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- 1️⃣ Título principal ---
            Text(
                text = "Reproceso ${reproceso.reprocesoNumber}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = TextSecondary.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 2️⃣ Métricas clave ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricItem(
                    icon = Icons.Default.Scale,
                    label = "Peso Total",
                    value = "${formatWeight(totalWeightNumber)} Kg",
                    iconColor = SecondaryColor
                )
                MetricItem(
                    icon = Icons.Outlined.ShoppingBag,
                    label = "BigBags",
                    value = bigBagCount.toString(),
                    iconColor = Color(0xFF00BFA5)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3️⃣ Información adicional ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Descripción / Material
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Descripción",
                        tint = PrimaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Material: ${reproceso.reprocesoDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Fecha del reproceso
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha reproceso",
                        tint = PrimaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha: ${formatInstant(reproceso.reprocesoDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                // Lote destino
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Lote destino",
                        tint = PrimaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lote destino: ${reproceso.reprocesoTargetLoteNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // --- 🧩 Diálogo con BigBags Reprocesados ---
    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
                }
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BigBags del Reproceso",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                BigBagDialogReproceso(bigBags = reproceso.reprocesoBigBag)
            }
        )
    }
}
