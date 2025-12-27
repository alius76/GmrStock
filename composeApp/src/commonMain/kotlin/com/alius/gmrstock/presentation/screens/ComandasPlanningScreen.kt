package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
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
import com.alius.gmrstock.core.utils.PdfGenerator
import com.alius.gmrstock.data.getComandaRepository
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.ui.components.PlanningItemCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import com.alius.gmrstock.data.getClientRepository
import com.alius.gmrstock.ui.components.PlanningAssignmentBottomSheet
import com.alius.gmrstock.core.utils.formatInstant


@OptIn(ExperimentalMaterial3Api::class)
class ComandasPlanningScreen(
    private val databaseUrl: String,
    private val currentUserEmail: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        // --- Repositorios, Estados, Loaders, Grouping, Sorting ---
        val comandaRepository = remember(databaseUrl) { getComandaRepository(databaseUrl) }
        val clientRepository = remember(databaseUrl) { getClientRepository(databaseUrl) }
        var comandasActivas by remember { mutableStateOf<List<Comanda>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showAssignmentBottomSheet by remember { mutableStateOf(false) }
        var selectedComandaForAssignment by remember { mutableStateOf<Comanda?>(null) }
        val assignmentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val snackbarHostState = remember { SnackbarHostState() }

        fun loadComandasActivas() {
            coroutineScope.launch {
                isLoading = true
                try {
                    val result = comandaRepository.listarTodasComandas()
                    comandasActivas = result
                } catch (e: Exception) {
                    comandasActivas = emptyList()
                } finally {
                    isLoading = false
                }
            }
        }

        val todayDate = remember {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }

        val groupedComandas by remember(comandasActivas) {
            derivedStateOf {
                val filtered = comandasActivas.filter { !it.fueVendidoComanda }
                filtered.groupBy { comanda: Comanda ->
                    comanda.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                }
            }
        }

        val sortedComandaEntries by remember(groupedComandas) {
            derivedStateOf {
                groupedComandas.entries.sortedBy { it.key }
            }
        }

        LaunchedEffect(databaseUrl) { loadComandasActivas() }
        // ---------------------------------------------------------------------------------


        Scaffold(
            containerColor = BackgroundColor,
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        "Planning de comandas",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    // 🔥 BOTÓN DE IMPRESIÓN PDF
                    IconButton(
                        onClick = {
                            if (comandasActivas.isNotEmpty()) {
                                // 1. Filtramos y ordenamos para obtener los extremos reales
                                val filteredAndSorted = comandasActivas
                                    .filter { !it.fueVendidoComanda }
                                    .sortedBy { it.dateBookedComanda }

                                val firstComanda = filteredAndSorted.firstOrNull()
                                val lastComanda = filteredAndSorted.lastOrNull()

                                // 2. Usamos tu función formatInstant para el formato DD/MM/AAAA
                                val startStr = formatInstant(firstComanda?.dateBookedComanda)
                                val endStr = formatInstant(lastComanda?.dateBookedComanda)

                                // 3. Construimos el texto del rango
                                val dateRange = if (startStr.isNotEmpty() && endStr.isNotEmpty()) {
                                    if (startStr == endStr) startStr else "$startStr al $endStr"
                                } else {
                                    "Fecha no definida"
                                }

                                // 4. Llamamos al generador
                                PdfGenerator.generatePlanningPdf(
                                    comandas = comandasActivas,
                                    title = "Planning de comandas",
                                    dateRange = dateRange
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Imprimir",
                            tint = PrimaryColor
                        )
                    }
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
                    Text(
                        "No hay reservas activas pendientes.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        sortedComandaEntries.forEach { entry ->
                            val date = entry.key
                            val comandasList = entry.value
                            val dateText = when (date) {
                                todayDate -> "HOY"
                                null -> "Fecha Desconocida"
                                else -> "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                dateText,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PrimaryColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "Comandas: ${comandasList.size}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            comandasList.forEach { comanda ->
                                                PlanningItemCard(
                                                    comanda = comanda,
                                                    onClick = { clickedComanda ->
                                                        selectedComandaForAssignment = clickedComanda
                                                        showAssignmentBottomSheet = true
                                                        coroutineScope.launch { assignmentSheetState.show() }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showAssignmentBottomSheet && selectedComandaForAssignment != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch {
                            assignmentSheetState.hide()
                            showAssignmentBottomSheet = false
                            selectedComandaForAssignment = null
                        }
                    },
                    sheetState = assignmentSheetState,
                    modifier = Modifier.fillMaxHeight(0.75f)
                ) {
                    PlanningAssignmentBottomSheet(
                        selectedComanda = selectedComandaForAssignment!!,
                        databaseUrl = databaseUrl,
                        currentUserEmail = currentUserEmail,
                        clientRepository = clientRepository,
                        snackbarHostState = snackbarHostState,
                        onLoteAssignmentSuccess = {
                            coroutineScope.launch {
                                assignmentSheetState.hide()
                                showAssignmentBottomSheet = false
                                selectedComandaForAssignment = null
                                loadComandasActivas()
                            }
                        }
                    )
                }
            }
        }
    }
}