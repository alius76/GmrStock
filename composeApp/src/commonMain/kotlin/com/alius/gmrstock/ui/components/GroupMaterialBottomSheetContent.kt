package com.alius.gmrstock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val loadLotesAndCertificados: suspend () -> Unit = {
        isLoading = true
        try {
            val loadedLotes = loteNumbers.mapNotNull { number -> loteRepository.getLoteByNumber(number) }
            // 🟢 Opcional: Ordenar lotes por número para una mejor visualización en columna
            lotes = loadedLotes.sortedBy { it.number }

            val certs = loadedLotes.associate { lote ->
                lote.number to certificadoRepository.getCertificadoByLoteNumber(lote.number)
            }
            certificados = certs
        } catch (e: Exception) {
            Napier.e("Error loading lotes/certs: ${e.message}", e)
            scope.launch {
                snackbarHostState.showSnackbar("Error al recargar los detalles del lote: ${e.message}")
            }
        } finally {
            isLoading = false
        }
    }

    // Precarga inicial
    LaunchedEffect(loteNumbers) {
        loadLotesAndCertificados()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // ❌ Altura fija de 520.dp eliminada o reemplazada por un mínimo,
            // ya que el ModalBottomSheet ya tiene fillMaxHeight(0.9f) y LazyColumn gestionará el scroll
            .fillMaxHeight()
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
                // 🟢 CAMBIO A LazyColumn (Lista Vertical)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Permite que la lista use el espacio restante
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(lotes, key = { it.id }) { lote ->
                        val cert = certificados[lote.number]

                        val certColor = when (cert?.status) {
                            CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                            CertificadoStatus.CORRECTO -> PrimaryColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        LoteCard(
                            lote = lote,
                            certificado = cert,
                            certificadoIconColor = certColor,
                            // 🟢 Modificadores ajustados para que ocupe el ancho completo
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLoteClick(lote) },
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            onViewBigBags = onViewBigBags,
                            databaseUrl = databaseUrl,
                            onRemarkUpdated = { updatedLote ->
                                // Actualiza la lista local y notifica al padre
                                lotes = lotes.map { if (it.id == updatedLote.id) updatedLote else it }
                                onRemarkUpdated(updatedLote)
                            },
                            clientRepository = clientRepository,
                            currentUserEmail = currentUserEmail
                        )
                    }
                }
            }
        }
    }
}