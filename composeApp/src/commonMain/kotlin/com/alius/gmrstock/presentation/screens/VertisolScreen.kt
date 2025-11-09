package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.formatWeight
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.Vertisol
import com.alius.gmrstock.ui.components.VertisolCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class VertisolScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val loteRepository = remember { getLoteRepository(databaseUrl) }

        var vertisolList by remember { mutableStateOf<List<Vertisol>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var totalKilos by remember { mutableStateOf(0.0) }

        // 🔹 Cargar los lotes Vertisol
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    isLoading = true
                    val loadedVertisol = loteRepository.listarLotesVertisol()
                    vertisolList = loadedVertisol
                    totalKilos = loadedVertisol.sumOf { it.vertisolTotalWeight.toDoubleOrNull() ?: 0.0 }
                } catch (e: Exception) {
                    println("❌ Error cargando Vertisol: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }

                vertisolList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay lotes ubicados en Vertisol.",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                else -> {
                    // --- LazyColumn con encabezado incluido ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 🔸 Header scrollable
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { navigator.pop() }) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Volver",
                                            tint = PrimaryColor
                                        )
                                    }
                                }

                                Text(
                                    text = "Lotes en Vertisol",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Text(
                                    text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                            }
                        }

                        // 🔸 Listado de Vertisoles
                        items(vertisolList) { vertisol ->
                            VertisolCard(vertisol = vertisol)
                        }

                        // 🔸 Spacer final
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}
