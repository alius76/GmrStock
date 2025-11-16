package com.alius.gmrstock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.ReservedColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.datetime.Instant

@Composable
fun ReservaCard(lote: LoteModel, onClick: (LoteModel) -> Unit) {

    // Extraer datos clave para la UI
    val clienteNombre = lote.booked?.cliNombre ?: "Cliente Desconocido"
    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0
    val bigBagCount = lote.bigBag.size
    val bookedByUser = lote.bookedByUser

    // Lógica para mostrar la fecha
    val dateText = lote.dateBooked?.let { date ->
        formatInstant(date)
    } ?: "Sin fecha"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { onClick(lote) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- 1️⃣ Encabezado de la Reserva (Cliente) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cliente
                Text(
                    text = clienteNombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Icono de Lote Reservado
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lote Reservado",
                    tint = ReservedColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 2️⃣ Información de la Reserva (Fecha y Usuario en una sola línea) ---

            Row(verticalAlignment = Alignment.CenterVertically) {

                // 📅 Fecha de Reserva
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Fecha Reserva",
                    tint = PrimaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reservado: $dateText",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                // 🔑 Separador y Usuario (Solo si existe el usuario)
                if (!bookedByUser.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "|",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // 👤 Reservado por:
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario que reservó",
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bookedByUser,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // Separador
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = TextSecondary.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 3️⃣ Detalles del Lote ---
            Text(
                text = "Lote ${lote.number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            // 📦 NUEVO: Descripción del Material (lote.description)
            Text(
                text = lote.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Metricas clave: Peso y BigBags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MetricItem(
                    icon = Icons.Default.Scale,
                    label = "Peso Total",
                    value = "${formatWeight(totalWeightNumber)} Kg",
                    iconColor = SecondaryColor
                )
                MetricItem(
                    icon = Icons.Default.ShoppingBag,
                    label = "BigBags",
                    value = bigBagCount.toString(),
                    iconColor = Color(0xFF00BFA5)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Observaciones de la reserva
            if (!lote.bookedRemark.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Observaciones",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Obs. Reserva: ${lote.bookedRemark}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}