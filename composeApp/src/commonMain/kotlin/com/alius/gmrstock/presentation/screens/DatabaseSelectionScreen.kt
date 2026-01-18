package com.alius.gmrstock.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
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
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import io.ktor.client.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    private val produccionObjetivoKg = 100_000f

    @Composable
    override fun Content() {
        var progressDB1 by remember { mutableStateOf(0f) }
        var progressDB2 by remember { mutableStateOf(0f) }

        val httpClient = remember { HttpClient() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            delay(100)

            scope.launch {
                Napier.d { "Iniciando carga de ratios para la base de datos P07" }
                val ratioRepository1 = RatioRepositoryImpl(httpClient, FirestoreUrls.DB1_URL)
                val ratios1 = ratioRepository1.listarRatiosDelDia()
                val pesoTotalHoy1 = ratios1.map { it.ratioTotalWeight.toFloatOrNull() ?: 0f }.sum()
                progressDB1 = (pesoTotalHoy1 / produccionObjetivoKg).coerceIn(0.15f, 1f)
            }

            scope.launch {
                Napier.d { "Iniciando carga de ratios para la base de datos P08" }
                val ratioRepository2 = RatioRepositoryImpl(httpClient, FirestoreUrls.DB2_URL)
                val ratios2 = ratioRepository2.listarRatiosDelDia()
                val pesoTotalHoy2 = ratios2.map { it.ratioTotalWeight.toFloatOrNull() ?: 0f }.sum()
                progressDB2 = (pesoTotalHoy2 / produccionObjetivoKg).coerceIn(0.15f, 1f)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título y subtítulo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GMR Stock",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestión de stock en tiempo real",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Encabezado de selección
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Seleccione base de datos",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGrayColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conexión a la planta de producción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Fila de botones adaptativos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DatabaseCardWithProcessStyle(
                        label = "P07",
                        progress = progressDB1,
                        onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL) },
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 140.dp).widthIn(max = 200.dp)
                    )
                    DatabaseCardWithProcessStyle(
                        label = "P08",
                        progress = progressDB2,
                        onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL) },
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 140.dp).widthIn(max = 200.dp)
                    )
                }

                // Este Spacer consume el espacio restante, empujando la versión hacia la parte inferior
                Spacer(modifier = Modifier.weight(1f))

                // Información de la versión (Ajuste de altura agresivo)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 💡 AJUSTE FINAL: Utilizamos 50.dp para garantizar que se vea
                        .padding(bottom = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(
                        modifier = Modifier.fillMaxWidth(0.6f).height(1.dp),
                        color = DarkGrayColor.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GMR Stock v1.1.0 build 21 | © 2026",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    @Composable
    fun DatabaseCardWithProcessStyle(
        label: String,
        progress: Float,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000)
        )

        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .height(200.dp)
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
                        .animateContentSize(tween(800)),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Factory,
                        contentDescription = "Icono Planta de Producción",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(72.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Nivel de Producción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }

                    LinearProgressIndicator(
                        progress = animatedProgress,
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