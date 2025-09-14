package com.alius.gmrstock.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.data.PlatformImageComposable
import com.alius.gmrstock.data.loadPlatformImage
import com.alius.gmrstock.data.FirestoreUrls
import com.alius.gmrstock.ui.theme.PrimaryColor

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // fondo consistente con otras pantallas
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
                    Text(
                        "LOGO",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 🔹 Texto central destacado + subtítulo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Seleccione base de datos",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conéctese a la planta deseada",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryColor.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 🔹 Botones estilizados en fila y centrados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DatabaseCardWithProcessStyle(
                        label = "P07",
                        progress = 0.7f,
                        onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) }
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    DatabaseCardWithProcessStyle(
                        label = "P08",
                        progress = 0.45f,
                        onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) }
                    )
                }
            }
        }
    }
}

@Composable
fun DatabaseCardWithProcessStyle(
    label: String,
    progress: Float,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .size(width = 160.dp, height = 200.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF029083), Color(0xFF00BFA5))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(tween(300)),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = label,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color.Yellow,
                    trackColor = Color(0x33FFFFFF)
                )
            }
        }
    }
}
