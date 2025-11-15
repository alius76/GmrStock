package com.alius.gmrstock.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.data.ClientRepository
import com.alius.gmrstock.data.getCertificadoRepository
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.CertificadoStatus
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GroupMaterialBottomSheetContent(
    loteNumbers: List<String>,
    onLoteClick: (LoteModel) -> Unit,
    onDismissRequest: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onViewBigBags: (List<BigBags>) -> Unit,
    databaseUrl: String,
    onRemarkUpdated: (LoteModel) -> Unit,
    clientRepository: ClientRepository,
    currentUserEmail: String
) {
    val scope = rememberCoroutineScope()
    val loteRepository = remember { getLoteRepository(databaseUrl) }
    val certificadoRepository = remember { getCertificadoRepository(databaseUrl) }

    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableStateOf(0) }

    // Carga inicial
    LaunchedEffect(loteNumbers) {
        isLoading = true
        try {
            val loadedLotes = loteNumbers.mapNotNull { loteRepository.getLoteByNumber(it) }
            lotes = loadedLotes
            certificados = loadedLotes.associate { it.number to certificadoRepository.getCertificadoByLoteNumber(it.number) }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error cargando lotes: ${e.message}")
            }
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Título ---
        Text(
            text = "Lotes disponibles",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (lotes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay lotes disponibles para este material.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // --- Card con animación ---
                val lote = lotes[currentIndex]
                val cert = certificados[lote.number]
                val certColor = when (cert?.status) {
                    CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                    CertificadoStatus.CORRECTO -> PrimaryColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        slideInHorizontally { fullWidth -> if (targetState > initialState) fullWidth else -fullWidth } +
                                fadeIn() with
                                slideOutHorizontally { fullWidth -> if (targetState > initialState) -fullWidth else fullWidth } +
                                fadeOut()
                    }
                ) { _ ->
                    LoteCard(
                        lote = lote,
                        certificado = cert,
                        certificadoIconColor = certColor,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .align(Alignment.Center),
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

                // --- Botón izquierda ---
                IconButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Anterior",
                        tint = if (currentIndex == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else PrimaryColor
                    )
                }

                // --- Botón derecha ---
                IconButton(
                    onClick = { if (currentIndex < lotes.size - 1) currentIndex++ },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Siguiente",
                        tint = if (currentIndex == lotes.size - 1) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else PrimaryColor
                    )
                }

                // --- Indicadores tipo puntos ---
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    lotes.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(4.dp)
                                .background(
                                    color = if (index == currentIndex) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                    }
                }
            }
        }
    }
}
