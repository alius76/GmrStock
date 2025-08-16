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
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMaterialBottomSheetContent(
    loteNumbers: List<String>,
    onLoteClick: (LoteModel) -> Unit,
    onDismissRequest: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onGeneratePdf: suspend (LoteModel) -> Unit,
    onViewBigBags: (List<BigBags>) -> Unit,
    databaseUrl: String
) {
    val coroutineScope = rememberCoroutineScope()
    val loteRepository = remember { getLoteRepository(databaseUrl) }

    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar LoteModel a partir de los números
    LaunchedEffect(loteNumbers) {
        coroutineScope.launch {
            val loadedLotes = loteNumbers.mapNotNull { number ->
                loteRepository.getLoteByNumber(number)
            }
            lotes = loadedLotes
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
        // --- Header con título y botón de cierre ---
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            lotes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                        LoteCard(
                            lote = lote,
                            modifier = Modifier
                                .width(300.dp)
                                .clickable { onLoteClick(lote) },
                            scope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            onGeneratePdf = onGeneratePdf,
                            onViewBigBags = onViewBigBags
                        )
                    }
                }
            }
        }
    }
}
