package com.alius.gmrstock.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
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
        // Título
        Text(
            text = "Lotes disponibles",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            // Contenedor carrusel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    }
                ) { index ->
                    val lote = lotes[index]
                    val cert = certificados[lote.number]
                    val certColor = when (cert?.status) {
                        CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                        CertificadoStatus.CORRECTO -> PrimaryColor
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(modifier = Modifier.padding(top = 12.dp, bottom = 64.dp)) {
                        LoteCard(
                            lote = lote,
                            certificado = cert,
                            certificadoIconColor = certColor,
                            modifier = Modifier.fillMaxWidth(0.8f),
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

                // Botón izquierda
                IconButton(
                    onClick = {
                        if (currentIndex > 0) currentIndex--
                    },
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
                    onClick = {
                        if (currentIndex < lotes.size - 1) currentIndex++
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).size(48.dp),
                    enabled = currentIndex < lotes.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Siguiente",
                        tint = if (currentIndex < lotes.size - 1) PrimaryColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
