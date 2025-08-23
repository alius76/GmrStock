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
            .width(180.dp)
            .height(220.dp)
            .padding(6.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Número de proceso
            Text(
                text = proceso.number,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )

            // NUEVO: Icono debajo del número de proceso
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Proceso",
                tint = PrimaryColor,
                modifier = Modifier.size(42.dp)
            )

            // Información del proceso
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = proceso.description,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Fecha: ${formatInstant(proceso.date)}", color = TextPrimary)
            }
        }
    }

    // Diálogo del proceso
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
