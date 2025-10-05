package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description // ⬅️ Nuevo ícono
import androidx.compose.material.icons.filled.Person      // ⬅️ Nuevo ícono
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // ⬅️ Importación necesaria para TextAlign.Center
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.core.utils.formatWeight // ⬅️ ¡Importación necesaria!
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.theme.PrimaryColor

@Composable
fun ClientCard(
    cliente: Cliente,
    venta: Venta,
    modifier: Modifier = Modifier
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    // ⬅️ CORRECCIÓN: Usamos Double para la suma y el formateo del peso.
    val cantidadBigBags = venta.ventaBigbags.size
    val pesoTotalDouble = venta.ventaBigbags.sumOf { it.ventaBbWeight.toDoubleOrNull() ?: 0.0 }

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
                        text = "Lista de BigBags",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                VentaBigBagsDialogContent(bigBags = venta.ventaBigbags)
            }
        )
    }

    Card(
        modifier = modifier
            .width(300.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {

            // 1. CABECERA REESTRUCTURADA: Lote Grande + "Botones" de Acción
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Número del lote más grande y centrado
                Text(
                    text = venta.ventaLote,
                    style = MaterialTheme.typography.headlineMedium, // Estética de LoteCard
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Botones/Íconos Informativos para rellenar el espacio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly, // Espacio uniforme
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Ícono Estático: Cliente (Informativo, representa la venta al cliente)
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Cliente",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )

                    // Ícono Estático: Documento (Informativo, representa la documentación de venta)
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Documento de Venta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )

                    // Botón Real: Ver BigBags
                    IconButton(
                        onClick = { showBigBagsDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ViewList,
                            contentDescription = "Ver BigBags",
                            tint = PrimaryColor, // Color principal para la acción
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } // Fin de Column(Cabecera)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp) // ⬅️ DIVIDER AÑADIDO
            Spacer(modifier = Modifier.height(12.dp))

            // --- Detalles ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Cliente", cliente.cliNombre) // Añadimos el cliente
                DetailRow("Material", venta.ventaMaterial ?: "Sin material")
                DetailRow("Venta", formatInstant(venta.ventaFecha))
                DetailRow("BigBags", cantidadBigBags.toString())
                // ⬅️ FORMATO DE PESO APLICADO Y CORREGIDO
                DetailRow("Peso total", "${formatWeight(pesoTotalDouble)} Kg", PrimaryColor)
            }
        }
    }
}