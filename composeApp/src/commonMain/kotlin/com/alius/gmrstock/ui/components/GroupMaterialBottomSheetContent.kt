package com.alius.gmrstock.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import io.github.aakira.napier.Napier
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

    val loadLotesAndCertificados: suspend () -> Unit = {
        isLoading = true
        try {
            val loadedLotes = loteNumbers.mapNotNull { number -> loteRepository.getLoteByNumber(number) }
            lotes = loadedLotes

            val certs = loadedLotes.associate { lote ->
                lote.number to certificadoRepository.getCertificadoByLoteNumber(lote.number)
            }
            certificados = certs
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error al recargar los detalles del lote: ${e.message}")
            }
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(loteNumbers) { loadLotesAndCertificados() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(vertical = 14.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lotes disponibles",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            lotes.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay lotes disponibles para este material.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Flecha izquierda
                    IconButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Anterior"
                        )
                    }

                    // Card con animación de transición
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
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        }
                    ) { _ ->
                        LoteCard(
                            lote = lote,
                            certificado = cert,
                            certificadoIconColor = certColor,
                            modifier = Modifier
                                .width(300.dp)
                                .clickable { onLoteClick(lote) },
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

                    // Flecha derecha
                    IconButton(
                        onClick = { if (currentIndex < lotes.size - 1) currentIndex++ },
                        enabled = currentIndex < lotes.size - 1,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Siguiente"
                        )
                    }
                }
            }
        }
    }
}
