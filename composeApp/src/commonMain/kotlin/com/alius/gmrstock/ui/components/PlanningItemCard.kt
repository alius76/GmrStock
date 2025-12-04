package com.alius.gmrstock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun PlanningItemCard(comanda: Comanda) {

    val lotNumber = comanda.numberLoteComanda.ifBlank { "SIN ASIGNAR" }
    val isAssigned = lotNumber != "SIN ASIGNAR"

    // 1. SOLUCIÓN BORDE CONSISTENTE: Eliminamos el color de fondo condicional
    // y usamos Color.White para evitar cualquier efecto visual que simule un borde grueso.
    val cardColor = Color.White
    val lotColor = if (isAssigned) PrimaryColor else Color.DarkGray

    // Formatear número de comanda a 6 dígitos (000008)
    val formattedComandaNumber = comanda.numeroDeComanda.toString().padStart(6, '0')

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        // ✅ Borde fino consistente de 1.dp
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Columna 1: Cliente, Material y Peso ---
            Column(modifier = Modifier.weight(1f)) {
                // Cliente (Principal)
                Text(
                    text = comanda.bookedClientComanda?.cliNombre ?: "Cliente Desconocido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Material
                Text(
                    text = comanda.descriptionLoteComanda,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 2. SOLUCIÓN ETIQUETA DE PESO: Añadimos "Peso total: "
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Peso total: ${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.Gray
                )
            }

            // --- Columna 2: Lote y Comanda Badge ---
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                // Lote (Tamaño ajustado)
                Text(
                    text = lotNumber,
                    fontSize = 18.sp,
                    fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Medium,
                    color = lotColor
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Comanda Badge
                Box(
                    modifier = Modifier
                        .background(color = PrimaryColor.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#$formattedComandaNumber",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // --- Observaciones (si existen) ---
        if (comanda.remarkComanda.isNotBlank()) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
            Text(
                text = "Obs: ${comanda.remarkComanda}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}