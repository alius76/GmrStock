package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.data.loadPlatformImage
import com.alius.gmrstock.data.PlatformImageComposable
import com.alius.gmrstock.data.FirestoreUrls
import com.alius.gmrstock.ui.theme.PrimaryColor

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White // 🔹 Fondo fijo blanco
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 Logo superior
                val logoImage = loadPlatformImage("logo.png")
                if (logoImage != null) {
                    PlatformImageComposable(
                        image = logoImage,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(250.dp)
                    )
                } else {
                    Text("LOGO", color = MaterialTheme.colorScheme.onBackground)
                }

                Spacer(modifier = Modifier.height(36.dp))

                // 🔹 Texto central destacado
                Text(
                    text = "Seleccione base de datos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 🔹 Botones estilizados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DatabaseCard(
                        label = "Planta 7",
                        onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) }
                    )
                    DatabaseCard(
                        label = "Planta 8",
                        onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) }
                    )
                }
            }
        }
    }
}

@Composable
fun DatabaseCard(
    label: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .size(width = 160.dp, height = 200.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White // 🔹 Botón con fondo blanco
        ),
        elevation = CardDefaults.elevatedCardElevation(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Icono de base de datos
            Icon(
                imageVector = Icons.Filled.Storage,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(72.dp) // icono grande
            )

            // 🔹 Texto de la planta
            Text(
                text = label,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )
        }
    }
}
