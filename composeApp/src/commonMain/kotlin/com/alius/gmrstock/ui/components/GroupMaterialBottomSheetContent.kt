package com.alius.gmrstock.ui.components

import LoteCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.data.getCertificadoRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

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
            val loadedLotes = loteNumbers.mapNotNull { number ->
                loteRepository.getLoteByNumber(number)
            }
            lotes = loadedLotes

            val certs = loadedLotes.associate { lote ->
                lote.number to certificadoRepository.getCertificadoByLoteNumber(lote.number)
            }
            certificados = certs

            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(vertical = 24.dp)
            .navigationBarsPadding()
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lotes Disponibles",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                        val certColor = when {
                            cert == null -> MaterialTheme.colorScheme.onSurfaceVariant // gris
                            cert.status == "w" -> MaterialTheme.colorScheme.error       // rojo
                            else -> PrimaryColor                                       // correcto
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
