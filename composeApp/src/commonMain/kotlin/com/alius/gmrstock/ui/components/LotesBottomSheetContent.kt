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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.alius.gmrstock.data.ClientRepository
import com.alius.gmrstock.data.LoteRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.CertificadoStatus
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.SecondaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LotesBottomSheetContent(
    loteRepository: LoteRepository,
    clientRepository: ClientRepository,
    databaseUrl: String,
    currentUserEmail: String,
    onViewBigBags: (List<BigBags>) -> Unit,
    onRemarkUpdated: (LoteModel) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var searchText by remember { mutableStateOf("") }
    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Búsqueda dinámica con debounce y filtro contains
    LaunchedEffect(searchText) {
        isLoading = true
        kotlinx.coroutines.delay(300) // debounce

        val allLotes = withContext(Dispatchers.IO) {
            try {
                loteRepository.listarLotes("") // obtenemos todos los lotes
            } catch (e: Exception) {
                emptyList<LoteModel>()
            }
        }

        lotes = if (searchText.isBlank()) {
            allLotes
        } else {
            allLotes.filter { it.number.contains(searchText, ignoreCase = true) }
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // --- Título centrado ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Búsqueda de lotes",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            )
        }

        // --- Caja de texto de búsqueda con ancho reducido ---
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Ingrese número de lote...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (lotes.isEmpty() && searchText.isNotBlank()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No se encontraron lotes para \"$searchText\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val pagerState = rememberPagerState(initialPage = 0) { lotes.size }

            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentPadding = PaddingValues(vertical = 60.dp)
            ) { index ->
                val lote = lotes[index]
                val cert: Certificado? = null // integrar tu repositorio de certificados si quieres
                val certColor = when (cert?.status) {
                    CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                    CertificadoStatus.CORRECTO -> PrimaryColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val pageOffset = pagerState.currentPage - index + pagerState.currentPageOffsetFraction

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
                    LoteCard(
                        lote = lote,
                        certificado = cert,
                        certificadoIconColor = certColor,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        onViewBigBags = onViewBigBags,
                        databaseUrl = databaseUrl,
                        onRemarkUpdated = { updatedLote ->
                            lotes = lotes.map { if (it.id == updatedLote.id) updatedLote else it }
                            onRemarkUpdated(updatedLote)
                        },
                        clientRepository = clientRepository,
                        currentUserEmail = currentUserEmail
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dots pager indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(lotes.size) { index ->
                    val isActive = pagerState.currentPage == index
                    val dotSize by animateDpAsState(
                        targetValue = if (isActive) 14.dp else 10.dp,
                        animationSpec = tween(250)
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isActive) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        animationSpec = tween(250)
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                            .clickable {
                                scope.launch { pagerState.scrollToPage(index) }
                            }
                    )
                }
            }
        }
    }
}
