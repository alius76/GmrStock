package com.alius.gmrstock.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.AsyncImage
import com.alius.gmrstock.data.FirestoreUrls
import com.alius.gmrstock.data.getDatabaseLogoPath
import kotlinx.coroutines.delay

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
                // Animación de fade-in para el logo
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(200)
                    visible = true
                }
                val alpha by animateFloatAsState(if (visible) 1f else 0f)

                // Logo centrado, sin fondo ni sombra de botón
                AsyncImage(
                    model = getDatabaseLogoPath("logo.png"),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(220.dp)
                        .alpha(alpha)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Texto principal refinado
                Text(
                    text = "Seleccione base de datos",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de selección con animación y sombra suave
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DatabaseLogoButton(
                        fileName = "gmr_stock_p07.png",
                        onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) }
                    )

                    DatabaseLogoButton(
                        fileName = "gmr_stock_p08.png",
                        onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) }
                    )
                }
            }
        }
    }
}

@Composable
fun DatabaseLogoButton(fileName: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f)
    val elevation by animateFloatAsState(if (pressed) 4.dp.value else 12.dp.value)

    Surface(
        modifier = Modifier
            .size(160.dp)
            .scale(scale)
            .shadow(elevation.dp, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        AsyncImage(
            model = getDatabaseLogoPath(fileName),
            contentDescription = fileName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
