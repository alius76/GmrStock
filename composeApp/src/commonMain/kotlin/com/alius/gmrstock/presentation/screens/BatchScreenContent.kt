package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.ui.components.LoteItem
import com.alius.gmrstock.ui.components.LoteItemSmall
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BatchScreenContent(user: User, databaseUrl: String) {
    val loteRepository = remember(databaseUrl) { getLoteRepository(databaseUrl) }
    var lotesHoy by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var ultimosLotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val hoyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(databaseUrl) {
        loading = true
        scope.launch {
            lotesHoy = loteRepository.listarLotesCreadosHoy()
            ultimosLotes = loteRepository.listarUltimosLotes(5)
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // mantengo padding vertical
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Sección Lotes creados hoy ---
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(50.dp))

                        Text(
                            text = "Lotes creados hoy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Número de lotes: ${lotesHoy.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (lotesHoy.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(120.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7F4))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Sin lotes",
                                        tint = Color(0xFF2E2E2E),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sin lotes hoy",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF2E2E2E)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            state = hoyListState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = hoyListState),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(lotesHoy) { lote ->
                                LoteItemSmall(lote)
                            }
                        }
                    }
                }

                // --- Sección Últimos lotes ---
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(16.dp)) // MÁS grande para bajar título

                        Text(
                            text = "Últimos lotes",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Número de registros: ${ultimosLotes.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                items(ultimosLotes) { lote ->
                    LoteItem(lote)
                }
            }
        }
    }
}
