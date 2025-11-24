package com.alius.gmrstock.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
// ------------------------------------------------------------------
// ------------------------------------------------------------------
import androidx.compose.ui.text.font.FontWeight


import com.alius.gmrstock.ui.theme.TextSecondary
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.theme.PrimaryColor

import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupClientBottomSheetContent(
    cliente: Cliente,
    ventas: List<Venta>,
    onDismissRequest: () -> Unit
) {

    if (ventas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().height(420.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay ventas disponibles para este cliente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0) { ventas.size }
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Título fijo ---
        Text(
            text = "Lotes vendidos",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentAlignment = Alignment.Center
        ) {


            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 60.dp)
            ) { index ->

                val pageOffset = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction)

                // Efectos de transición de la card
                val scale by animateFloatAsState(
                    targetValue = lerp(0.85f, 1f, 1f - abs(pageOffset)),
                    animationSpec = tween(300)
                )

                val alpha by animateFloatAsState(
                    targetValue = lerp(0.55f, 1f, 1f - abs(pageOffset)),
                    animationSpec = tween(300)
                )

                val translation by animateFloatAsState(
                    targetValue = pageOffset * 40f,
                    animationSpec = tween(300)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            translationY = translation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Se asume que ClientCard existe en tu proyecto
                    ClientCard(
                        cliente = cliente,
                        venta = ventas[index],
                        modifier = Modifier.fillMaxWidth(0.85f) // La tarjeta ahora tiene el ancho adecuado.
                    )
                }
            }

            // ⚙️ BARRA VERTICAL DE PROGRESO (Scrollbar Deslizante)
            val totalItems = ventas.size
            if (totalItems > 1) {
                val barWidth = 4.dp
                val indicatorHeightDp = 420.dp
                val minThumbHeight = 20.dp

                // Altura del "Pulgar" (Scroll Thumb)
                val thumbHeight = (indicatorHeightDp / totalItems.toFloat()).coerceAtLeast(minThumbHeight)

                // Posición de desplazamiento normalizada (0.0 al 1.0)
                val currentPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
                val normalizedPosition = currentPosition / (totalItems - 1).toFloat()

                // Rango de movimiento del pulgar en Píxeles: (Altura total del Box - Altura del pulgar)
                val travelRangePx = with(density) { (indicatorHeightDp - thumbHeight).toPx() }

                // Desplazamiento animado (Offset)
                val thumbOffsetPx by animateFloatAsState(
                    targetValue = normalizedPosition * travelRangePx,
                    animationSpec = tween(300)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(barWidth)
                        .align(Alignment.CenterEnd)
                        .padding(vertical = 10.dp) // <--- 2. CORRECCIÓN: ELIMINADO padding horizontal extra (horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    // Pulgar Indicador
                    Box(
                        modifier = Modifier
                            .offset(y = with(density) { thumbOffsetPx.toDp() })
                            .width(barWidth)
                            .height(thumbHeight)
                            .clip(CircleShape)
                            .background(PrimaryColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}