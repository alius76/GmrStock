package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.utils.PdfGenerator
import com.alius.gmrstock.core.utils.formatInstant
import com.alius.gmrstock.data.getComandaRepository
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.domain.model.PlanningFilter
import com.alius.gmrstock.ui.components.PlanningItemCard
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.ReservedColor
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
class ComandasPlanningScreen(
    private val databaseUrl: String,
    private val currentUserEmail: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val comandaRepository = remember(databaseUrl) { getComandaRepository(databaseUrl) }
        var comandasActivas by remember { mutableStateOf<List<Comanda>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var selectedFilter by remember { mutableStateOf(PlanningFilter.TODAS) }

        fun loadComandasActivas() {
            coroutineScope.launch {
                isLoading = true
                try {
                    comandasActivas = comandaRepository.listarTodasComandas()
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

        val sortedGroups by remember(comandasActivas, selectedFilter) {
            derivedStateOf {
                val now = todayDate
                val filtered = comandasActivas.filter { comanda ->
                    if (comanda.fueVendidoComanda) return@filter false
                    val cDate = comanda.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                        ?: return@filter selectedFilter == PlanningFilter.TODAS

                    when (selectedFilter) {
                        PlanningFilter.TODAS -> true
                        PlanningFilter.SEMANA -> {
                            val daysFromMonday = now.dayOfWeek.ordinal
                            val currentMonday = now.minus(DatePeriod(days = daysFromMonday))
                            val nextSunday = currentMonday.plus(DatePeriod(days = 6))
                            cDate in currentMonday..nextSunday
                        }
                        PlanningFilter.MES -> cDate.month == now.month && cDate.year == now.year
                    }
                }
                filtered.groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
                    .entries.sortedBy { it.key }
            }
        }

        LaunchedEffect(Unit) { loadComandasActivas() }

        Scaffold(
            containerColor = BackgroundColor,
            topBar = {
                Surface(shadowElevation = 3.dp, color = BackgroundColor) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Atrás", tint = PrimaryColor)
                        }
                        Text(
                            "Planning Global",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val currentList = sortedGroups.flatMap { it.value }
                            if (currentList.isNotEmpty()) {
                                val sorted = currentList.sortedBy { it.dateBookedComanda }
                                val pdfTitle = when(selectedFilter) {
                                    PlanningFilter.TODAS -> "Planning General"
                                    PlanningFilter.SEMANA -> "Planning Semanal"
                                    PlanningFilter.MES -> "Planning Mensual"
                                }
                                PdfGenerator.generatePlanningPdf(
                                    comandas = currentList,
                                    title = pdfTitle,
                                    dateRange = "${formatInstant(sorted.first().dateBookedComanda)} al ${formatInstant(sorted.last().dateBookedComanda)}"
                                )
                            }
                        }) {
                            Icon(Icons.Default.Print, "Imprimir", tint = PrimaryColor)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlanningFilter.entries.forEach { filter ->
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.name,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor, strokeWidth = 3.dp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(sortedGroups) { (date, comandasList) ->
                            val isPast = date != null && date < todayDate
                            val dateText = when (date) {
                                todayDate -> "HOY"
                                null -> "SIN FECHA"
                                else -> "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Encabezado del Grupo
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (isPast) ReservedColor else PrimaryColor,
                                                    shape = RoundedCornerShape(50)
                                                )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (isPast) ReservedColor else Color.DarkGray
                                        )

                                        // Etiqueta RETRASADA junto a la fecha
                                        if (isPast) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                color = ReservedColor,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "RETRASO",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            "${comandasList.size} comandas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        comandasList.forEach { comanda ->
                                            PlanningItemCard(
                                                comanda = comanda,
                                                modifier = Modifier
                                                    .weight(1f, fill = true)
                                                    .fillMaxWidth(if (comandasList.size == 1) 1f else 0.485f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}