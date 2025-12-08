package com.alius.gmrstock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.ReservedColor


@Composable
fun ComandaCard(
    comanda: Comanda,
    isSelected: Boolean = false,
    onClick: (Comanda) -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onReassign: (() -> Unit)? = null,
    onEditRemark: (() -> Unit)? = null
) {
    val cardBackground = if (comanda.fueVendidoComanda) ReservedColor.copy(alpha = 0.02f) else Color.Transparent
    val bookedClientColor = if (comanda.fueVendidoComanda) Color.LightGray else MaterialTheme.colorScheme.secondary

    val rippleColor = if (comanda.fueVendidoComanda) ReservedColor.copy(alpha = 0.2f)
    else PrimaryColor.copy(alpha = 0.2f)

    val iconTint = when {
        comanda.fueVendidoComanda -> ReservedColor
        comanda.numberLoteComanda.isBlank() -> Color.Gray
        else -> PrimaryColor
    }

    // Color del badge para Vendida -> Gris Oscuro
    val badgeColor = if (comanda.fueVendidoComanda) Color.DarkGray else PrimaryColor

    val formattedComandaNumber = comanda.numeroDeComanda.toString().padStart(6, '0')

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick(comanda) },
                indication = rememberRipple(bounded = true, color = rippleColor),
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // --- Card principal (Estructura de la comanda) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    // 🔥 PEQUEÑO AJUSTE DE PADDING SUPERIOR para dar más espacio
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {

                    comanda.bookedClientComanda?.let { cliente ->
                        Text(
                            text = cliente.cliNombre.ifBlank { "Cliente sin nombre" },
                            style = MaterialTheme.typography.titleMedium,
                            color = bookedClientColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = comanda.descriptionLoteComanda,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Peso total: ${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    if (comanda.remarkComanda.isNotBlank()) {
                        Text(
                            text = "Obs: ${comanda.remarkComanda}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = comanda.numberLoteComanda.ifBlank { "Sin asignar lote" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (comanda.fueVendidoComanda) {
                        Text(
                            text = "Vendido",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReservedColor
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Asignado",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- Badge superior izquierdo ---
        if (comanda.numeroDeComanda > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    // 🔥 CORRECCIÓN: Aumentamos el desplazamiento vertical para separarlo del contenido
                    .offset(x = 8.dp, y = (-12).dp)
                    .background(color = badgeColor, shape = CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "#$formattedComandaNumber",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Overlay de Botones de Acción ---
        if (isSelected && !comanda.fueVendidoComanda) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Botón de Eliminar/Anular (Solo si no tiene lote asignado)
                    if (comanda.numberLoteComanda.isBlank() && onDelete != null) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = ReservedColor)
                        ) { Text("Anular") }
                    }

                    // 2. Botón de Reasignar
                    if (onReassign != null) {
                        Button(
                            onClick = onReassign,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) { Text("Reasignar") }
                    }

                    // 3. Botón de Observaciones
                    onEditRemark?.let {
                        Button(
                            onClick = it,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF09A00))
                        ) {
                            Icon(Icons.Default.Notes, contentDescription = "Editar observaciones", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Obs.")
                        }
                    }
                }
            }
        }
    }
}