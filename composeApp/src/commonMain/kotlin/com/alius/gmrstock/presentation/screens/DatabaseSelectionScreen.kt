package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
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
                            .fillMaxWidth(0.95f)
                            .height(220.dp)
                    )
                } else {
                    Text("LOGO", color = MaterialTheme.colorScheme.onBackground)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Seleccione base de datos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 🔹 Botones con imágenes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val db1Image = loadPlatformImage("gmr_stock_p07.png")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DatabaseLogoButton(
                            image = db1Image,
                            label = "DB1",
                            onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) }
                        ) {
                            Text("Seleccionar")
                        }
                    }

                    val db2Image = loadPlatformImage("gmr_stock_p08.png")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DatabaseLogoButton(
                            image = db2Image,
                            label = "DB2",
                            onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) }
                        ) {
                            Text("Seleccionar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseLogoButton(
    image: Any?,
    label: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.size(160.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                PlatformImageComposable(
                    image = image,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

