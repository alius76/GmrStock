package com.alius.gmrstock.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.data.FirestoreUrls
import com.alius.gmrstock.data.RatioRepositoryImpl
import com.alius.gmrstock.ui.theme.DarkGrayColor
import com.alius.gmrstock.ui.theme.GrayColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import io.ktor.client.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    private val produccionObjetivoKg = 100_000f

    @Composable
    override fun Content() {
        var progressDB1 by remember { mutableStateOf(0.15f) }
        var progressDB2 by remember { mutableStateOf(0.15f) }

        val httpClient = remember { HttpClient() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            // Carga de ratios para la primera base de datos
            scope.launch {
                Napier.d { "Iniciando carga de ratios para la base de datos P07" }
                val ratioRepository1 = RatioRepositoryImpl(httpClient, FirestoreUrls.DB1_URL)
                val ratios1 = ratioRepository1.listarRatiosDelDia()

                Napier.d { "P07 - Ratios obtenidos: ${ratios1.size}" }

                val pesoTotalHoy1 = ratios1.map {
                    it.ratioTotalWeight.toFloatOrNull() ?: 0f
                }.sum()

                Napier.d { "P07 - Peso total producido hoy: $pesoTotalHoy1 kg" }

                progressDB1 = (pesoTotalHoy1 / produccionObjetivoKg).coerceIn(0.15f, 1f)

                Napier.d { "P07 - Progreso final de la barra: $progressDB1" }
            }

            // Carga de ratios para la segunda base de datos
            scope.launch {
                Napier.d { "Iniciando carga de ratios para la base de datos P08" }
                val ratioRepository2 = RatioRepositoryImpl(httpClient, FirestoreUrls.DB2_URL)
                val ratios2 = ratioRepository2.listarRatiosDelDia()

                Napier.d { "P08 - Ratios obtenidos: ${ratios2.size}" }

                val pesoTotalHoy2 = ratios2.map {
                    it.ratioTotalWeight.toFloatOrNull() ?: 0f
                }.sum()

                Napier.d { "P08 - Peso total producido hoy: $pesoTotalHoy2 kg" }

                progressDB2 = (pesoTotalHoy2 / produccionObjetivoKg).coerceIn(0.15f, 1f)

                Napier.d { "P08 - Progreso final de la barra: $progressDB2" }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título y subtítulo de la app
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GMR Stock",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGrayColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestión de stock en tiempo real.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))

                // Encabezado de la pantalla de selección
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Seleccione base de datos",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGrayColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conéctese a la planta deseada",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DatabaseCardWithProcessStyle(
                        label = "P07",
                        progress = progressDB1,
                        onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) }
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    DatabaseCardWithProcessStyle(
                        label = "P08",
                        progress = progressDB2,
                        onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) }
                    )
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
                    // Cambiar a Factory si tienes material-icons-extended
                    Icon(
                        imageVector = Icons.Filled.Factory,
                        contentDescription = "Icono Planta de Producción",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(72.dp)
                    )

                    // Texto centrado
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Planta de Producción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }

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


}