package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.data.*
import com.alius.gmrstock.domain.model.TraceEvent
import com.alius.gmrstock.ui.components.TraceEventCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class TrazabilidadScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        // --- REPOSITORIOS ---
        val loteRepo = remember(databaseUrl) { getLoteRepository(databaseUrl) }
        val ventaRepo = remember(databaseUrl) { getVentaRepository(databaseUrl) }
        val reproRepo = remember(databaseUrl) { getReprocesarRepository(databaseUrl) }
        val devRepo = remember(databaseUrl) { getDevolucionRepository(databaseUrl) }
        val historialRepo = remember(databaseUrl) { getHistorialRepository(databaseUrl) }

        // --- CASO DE USO ---
        val traceUseCase = remember {
            GetLoteTraceUseCase(loteRepo, historialRepo, ventaRepo, reproRepo, devRepo)
        }

        // --- ESTADOS ---
        var numeroLote by remember { mutableStateOf("") }
        var timelineEvents by remember { mutableStateOf<List<TraceEvent>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isFirstLoad by remember { mutableStateOf(true) }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {

            // --- HEADER FIJO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                        Text(
                            text = "Trazabilidad",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Historial completo del lote",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray
                        )
                    }
                }
            }

            // --- CUERPO SCROLLABLE ---
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // SECCIÓN DE BÚSQUEDA
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = numeroLote,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } || input.isEmpty()) numeroLote = input
                            },
                            label = { Text("Número de lote") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = PrimaryColor,
                                focusedLabelColor = PrimaryColor
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    isFirstLoad = false
                                    errorMessage = null
                                    timelineEvents = emptyList()
                                    try {
                                        val result = traceUseCase.execute(numeroLote)
                                        // Si result viene vacío, el estado 'timelineEvents'
                                        // se mantiene vacío y el 'when' inferior muestra el EmptyState.
                                        timelineEvents = result
                                    } catch (e: Exception) {
                                        errorMessage = "❌ Error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            enabled = numeroLote.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Rastrear lote", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- GESTIÓN DE CONTENIDO PRINCIPAL ---
                when {
                    isLoading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryColor)
                            }
                        }
                    }
                    isFirstLoad -> {
                        item { EmptyStateTrazabilidad(isInitial = true) }
                    }
                    errorMessage != null -> {
                        item {
                            Surface(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = Color.Red,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    timelineEvents.isEmpty() -> {
                        // Se muestra después de buscar si no hay resultados y no hay error técnico
                        item { EmptyStateTrazabilidad(isInitial = false) }
                    }
                    else -> {
                        items(timelineEvents) { event ->
                            TraceEventCard(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateTrazabilidad(isInitial: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isInitial) Icons.Default.Search else Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp).alpha(0.2f),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isInitial) {
                "Funcionalidad de trazabilidad en estado de pre-alpha"
            } else {
                "No se encontraron eventos para este lote"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}