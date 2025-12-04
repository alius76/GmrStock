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
import com.alius.gmrstock.data.getComandaRepository
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.ui.components.PlanningItemCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.*


class ComandasPlanningScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val comandaRepository = remember(databaseUrl) { getComandaRepository(databaseUrl) }

        // Estado de la lista completa de comandas activas
        var comandasActivas by remember { mutableStateOf<List<Comanda>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        // Optimización: Calcular la fecha de hoy una sola vez en Compose
        val todayDate = remember {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }

        // Función de agrupamiento
        val groupedComandas by remember(comandasActivas) {
            derivedStateOf {
                println(">>> [PLANNING] 2. Ejecutando agrupamiento. Total de comandasActivas: ${comandasActivas.size}")

                val filtered = comandasActivas
                    .filter { !it.fueVendidoComanda }

                println(">>> [PLANNING] 3. Comandas no vendidas (fueVendidoComanda=false): ${filtered.size}")

                val grouped = filtered
                    .groupBy { comanda: Comanda ->
                        comanda.dateBookedComanda
                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                            ?.date
                    }

                println(">>> [PLANNING] 4. Grupos de comandas creados (Map size): ${grouped.size}")
                grouped
            }
        }

        // NUEVO: Ordenar las entradas del mapa (LocalDate a List<Comanda>)
        val sortedComandaEntries by remember(groupedComandas) {
            derivedStateOf {
                val sorted = groupedComandas.entries.sortedBy { it.key }
                println(">>> [PLANNING] 5. Entradas ordenadas. Total de entradas: ${sorted.size}")
                sorted
            }
        }


        // Carga de datos
        LaunchedEffect(databaseUrl) {
            println(">>> [PLANNING] 1. Iniciando carga de datos...")
            isLoading = true
            try {
                val result = comandaRepository.listarTodasComandas()
                println(">>> [PLANNING] 1b. Repositorio devolvió ${result.size} comandas.")
                comandasActivas = result
            } catch (e: Exception) {
                println(">>> [PLANNING] ❌ ERROR al cargar comandas: ${e.message}")
                comandasActivas = emptyList()
            } finally {
                isLoading = false
                println(">>> [PLANNING] 1c. Carga finalizada. isLoading = false")
            }
        }

        Scaffold(
            containerColor = BackgroundColor,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Planning de reservas activas",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryColor)
                }
                else if (sortedComandaEntries.isEmpty()) {
                    println(">>> [PLANNING] 6. Mostrando: No hay reservas activas pendientes. (Lista vacía)")
                    Text(
                        "No hay reservas activas pendientes.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = Color.Gray
                    )
                } else {
                    println(">>> [PLANNING] 6. Mostrando: Lista de comandas. Total de grupos: ${sortedComandaEntries.size}")
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sortedComandaEntries.forEach { entry ->
                            val date = entry.key
                            val comandasList = entry.value

                            // 🌟 Encabezado de Fecha
                            item {
                                Spacer(modifier = Modifier.height(8.dp))

                                val dateText = when (date) {
                                    todayDate -> "HOY"
                                    null -> "Fecha Desconocida"
                                    else -> "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
                                }

                                println(">>> [PLANNING] Mostrando grupo: $dateText con ${comandasList.size} items")

                                Text(
                                    dateText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Divider(color = PrimaryColor.copy(alpha = 0.5f))
                            }

                            // 🌟 Items de Comanda
                            items(comandasList) { comanda ->
                                // Solo imprimir IDs si es necesario, si no, solo la existencia del loop
                                // println(">>> [PLANNING] Mostrando item: ${comanda.idComanda}")
                                PlanningItemCard(comanda = comanda)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}