package com.alius.gmrstock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    databaseUrl: String
) {
    val scope = rememberCoroutineScope()
    val loteRepository = remember { getLoteRepository(databaseUrl) }
    val certificadoRepository = remember { getCertificadoRepository(databaseUrl) }

    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Precarga de lotes + certificados
    LaunchedEffect(loteNumbers) {
        scope.launch {
            Napier.i(message = "▶️ [BottomSheet] Iniciando carga para lotes: $loteNumbers")
            val loadedLotes = loteNumbers.mapNotNull { number -> loteRepository.getLoteByNumber(number) }
            lotes = loadedLotes
            Napier.i(message = "✅ [BottomSheet] Lotes cargados. Total: ${lotes.size}")

            val certs = loadedLotes.associate { lote ->
                Napier.i(message = "🔍 [BottomSheet] Buscando certificado para lote: ${lote.number}")
                val cert = certificadoRepository.getCertificadoByLoteNumber(lote.number)
                Napier.i(message = "🔗 [BottomSheet] Certificado para ${lote.number} encontrado: ${cert?.loteNumber ?: "null"}")
                lote.number to cert
            }
            certificados = certs
            Napier.i(message = "📈 [BottomSheet] Mapa de certificados cargado. Total: ${certificados.size}")

            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(vertical = 14.dp)
            .navigationBarsPadding()
    ) {
        // --- Header centrado ---
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
                    CircularProgressIndicator()
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
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lotes) { lote ->
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
                            modifier = Modifier
                                .width(300.dp)
                                .clickable { onLoteClick(lote) },
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            onViewBigBags = onViewBigBags,
                            databaseUrl = databaseUrl
                        )
                    }
                }
            }
        }
    }
}