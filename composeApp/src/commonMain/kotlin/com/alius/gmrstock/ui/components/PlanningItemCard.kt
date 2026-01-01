package com.alius.gmrstock.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.WarningColor

@Composable
fun PlanningItemCard(
    comanda: Comanda,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // --- Lógica de Estado ---
    val isAssigned = comanda.numberLoteComanda.isNotBlank()
    val lotNumber = if (isAssigned) comanda.numberLoteComanda else "PENDIENTE"

    // Usamos PrimaryColor (Verde/Azul) si tiene lote, y WarningColor (Ámbar) si está pendiente
    val indicatorColor = if (isAssigned) PrimaryColor else WarningColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // --- Barra de Color Lateral (Refleja estado de lote) ---
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                // Nombre del Cliente
                Text(
                    text = comanda.bookedClientComanda?.cliNombre?.uppercase() ?: "SIN CLIENTE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción del Material
                Text(
                    text = comanda.descriptionLoteComanda,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fila Inferior: Etiqueta de Lote y Peso Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Badge con el número de Lote o texto "PENDIENTE"
                    Surface(
                        color = indicatorColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = lotNumber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = indicatorColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Peso formateado
                    Text(
                        text = "${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                // --- Sección de Observaciones (Solo si existen y está expandido) ---
                AnimatedVisibility(
                    visible = isExpanded && comanda.remarkComanda.isNotBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "Observaciones:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Text(
                            text = comanda.remarkComanda,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}