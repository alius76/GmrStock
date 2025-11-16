package com.alius.gmrstock.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GroupClientBottomSheetContent(
    cliente: Cliente,
    ventas: List<Venta>,
    onDismissRequest: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Header centrado ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lotes vendidos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            ventas.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay ventas disponibles para este cliente.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                // Carrusel con puntos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentIndex,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing)) +
                                        scaleIn(initialScale = 0.96f, animationSpec = tween(450, easing = FastOutSlowInEasing)) with
                                        fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing))
                            }
                        ) { index ->
                            val venta = ventas[index]
                            ClientCard(
                                cliente = cliente,
                                venta = venta,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }

                        // Botón izquierda
                        IconButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            modifier = Modifier.align(Alignment.CenterStart).size(48.dp),
                            enabled = currentIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Anterior",
                                tint = if (currentIndex > 0) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        // Botón derecha
                        IconButton(
                            onClick = { if (currentIndex < ventas.size - 1) currentIndex++ },
                            modifier = Modifier.align(Alignment.CenterEnd).size(48.dp),
                            enabled = currentIndex < ventas.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Siguiente",
                                tint = if (currentIndex < ventas.size - 1) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Barra de puntos con animación mejorada ---
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ventas.forEachIndexed { index, _ ->
                            val isActive = index == currentIndex

                            val dotSize by animateDpAsState(
                                targetValue = if (isActive) 14.dp else 10.dp,
                                animationSpec = tween(250)
                            )

                            val dotColor by animateColorAsState(
                                targetValue = if (isActive) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                animationSpec = tween(250)
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(dotSize)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .clickable { currentIndex = index }
                            )
                        }
                    }
                }
            }
        }
    }
}
