package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.launch
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
            modifier = Modifier.fillMaxSize(),
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
    val scope = rememberCoroutineScope() // ← necesario para scrollToPage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Título fijo ---
        Text(
            text = "Lotes vendidos",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentAlignment = Alignment.Center
        ) {

            // === PAGER VERTICAL con padding para mostrar cards vecinas ===
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 60.dp)
            ) { index ->

                val pageOffset = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction)

                // Escala proporcional basada en distancia al centro
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
                    ClientCard(
                        cliente = cliente,
                        venta = ventas[index],
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // === PUNTOS INFERIORES con función de clickable para navegar ===
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(ventas.size) { index ->
                val isActive = pagerState.currentPage == index

                val dotSize by animateDpAsState(
                    targetValue = if (isActive) 14.dp else 10.dp,
                    animationSpec = tween(250)
                )

                val dotColor by animateColorAsState(
                    targetValue = if (isActive) PrimaryColor
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    animationSpec = tween(250)
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable {
                            scope.launch {
                                pagerState.scrollToPage(index) // ← mover a la card correspondiente
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
