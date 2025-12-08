package com.alius.gmrstock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.clickable
import com.alius.gmrstock.ui.theme.ReservedColor


@Composable
fun PlanningItemCard(
    comanda: Comanda,
    onClick: (Comanda) -> Unit
) {
    val lotNumber = comanda.numberLoteComanda.ifBlank { "SIN ASIGNAR" }
    val isAssigned = lotNumber != "SIN ASIGNAR"
    val formattedComandaNumber = comanda.numeroDeComanda.toString().padStart(6, '0')


    val indicatorColor = if (isAssigned) PrimaryColor else ReservedColor

    // Usamos Box para superponer el indicador lateral
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(comanda) }
    ) {
        // --- Indicador lateral prominente (La Clave Visual) ---
        Spacer(
            modifier = Modifier
                .width(6.dp) // Ancho del indicador
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(indicatorColor, shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
        )

        // --- Card Principal ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp), // Compensamos el ancho del indicador
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // 1. Cabecera Lote/Comanda y Botón
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Estado de Asignación (Más prominente)
                    Text(
                        text = "Lote: $lotNumber",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = indicatorColor
                    )

                    // Comanda Badge
                    Box(
                        modifier = Modifier
                            .background(color = PrimaryColor.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#$formattedComandaNumber",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 2. Detalles del Cuerpo (Cliente, Material, Peso)
                Column(modifier = Modifier.padding(16.dp)) {
                    // Cliente (Principal)
                    Text(
                        text = comanda.bookedClientComanda?.cliNombre ?: "Cliente Desconocido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                    Spacer(modifier = Modifier.height(2.dp))

                    // Peso
                    Text(
                        text = "Peso total: ${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.Gray
                    )
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
    }
}