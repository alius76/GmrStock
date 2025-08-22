package com.alius.gmrstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.domain.model.Process
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextPrimary

@Composable
fun ProcessItem(proceso: Process, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila superior: Número de proceso
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Proceso: ${proceso.number}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                // Opcional: algún badge si es necesario
                // Box(...) { Text(...) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de información con icono
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Proceso",
                    tint = PrimaryColor,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = "Descripción: ${proceso.description}", color = TextPrimary)
                    Text(text = "Fecha: ${formatInstant(proceso.date)}", color = TextPrimary)
                }
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
                    text = "Detalle del proceso",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Número: ${proceso.number}", color = TextPrimary)
                    Text("Descripción: ${proceso.description}", color = TextPrimary)
                    Text("Fecha: ${formatInstant(proceso.date)}", color = TextPrimary)
                }
            }
        )
    }
}
