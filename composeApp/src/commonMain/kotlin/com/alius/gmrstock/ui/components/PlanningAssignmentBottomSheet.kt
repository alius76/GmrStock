package com.alius.gmrstock.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.util.lerp
import com.alius.gmrstock.data.ClientRepository
import com.alius.gmrstock.data.getCertificadoRepository
import com.alius.gmrstock.data.getComandaRepository
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.*
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanningAssignmentBottomSheet(
    selectedComanda: Comanda,
    databaseUrl: String,
    currentUserEmail: String,
    clientRepository: ClientRepository,
    onLoteAssignmentSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val loteRepository = remember { getLoteRepository(databaseUrl) }
    val comandaRepository = remember { getComandaRepository(databaseUrl) }
    val materialDescription = remember { selectedComanda.descriptionLoteComanda }
    val comandaClientName = remember { selectedComanda.bookedClientComanda?.cliNombre ?: "Cliente Desconocido" }
    val density = LocalDensity.current

    var lotesDisponibles by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val certificadoRepository = remember { getCertificadoRepository(databaseUrl) }
    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }

    // Altura fija del Box del Pager
    val pagerBoxHeightDp = 420.dp

    // Función de carga centralizada (maneja la cancelación silenciosa)
    val loadLotesForAssignment: () -> Unit = {
        scope.launch {
            isLoading = true
            try {
                // 1. Cargamos los lotes por descripción
                val loadedLotes = loteRepository.listarLotesPorDescripcion(materialDescription)

                // 2. 🔥 NUEVA LÓGICA: Cargamos todas las comandas para ver cuáles lotes están ya "comprometidos"
                val todasLasComandas = comandaRepository.listarTodasComandas()

                // Creamos un Set de números de lote que ya están asignados a OTRAS comandas
                val lotesOcupadosEnOtrasComandas = todasLasComandas
                    .filter { it.idComanda != selectedComanda.idComanda && it.numberLoteComanda.isNotBlank() }
                    .map { it.numberLoteComanda }
                    .toSet()

                var filteredLotes = loadedLotes
                    .filter { lote ->
                        val isDescriptionMatch = lote.description.equals(materialDescription, ignoreCase = true)
                        val bookedClient = lote.booked?.cliNombre

                        // Verificamos si el lote está ocupado por otra comanda activa
                        val estaOcupadoPorOtraComanda = lotesOcupadosEnOtrasComandas.contains(lote.number)

                        // Si la comanda actual ya tiene un lote asignado, solo mostramos ese (tu lógica de claridad)
                        if (selectedComanda.numberLoteComanda.isNotBlank()) {
                            lote.number == selectedComanda.numberLoteComanda
                        } else {
                            // Filtro de disponibilidad:
                            // - Debe coincidir la descripción
                            // - NO debe estar asignado a otra comanda activa (incluso si es del mismo cliente)
                            // - Debe estar sin reserva O reservado por el cliente actual
                            val isAvailableOrReservedByMe = (lote.booked == null || bookedClient == comandaClientName)

                            isDescriptionMatch && isAvailableOrReservedByMe && !estaOcupadoPorOtraComanda
                        }
                    }

                filteredLotes = filteredLotes.sortedBy { it.number }
                lotesDisponibles = filteredLotes
                certificados = lotesDisponibles.associate { it.number to certificadoRepository.getCertificadoByLoteNumber(it.number) }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    // Manejo silencioso de la cancelación
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Error cargando lotes: ${e.message}") }
                }
            } finally {
                if (scope.isActive) {
                    isLoading = false
                }
            }
        }
    }


    // 🔥 MODIFICACIÓN CLAVE: Función centralizada de asignación/anulación
    val assignLoteToComanda: (LoteModel, Boolean) -> Unit = { loteToProcess, shouldClearBooking ->
        scope.launch {
            val cliente = selectedComanda.bookedClientComanda
            val comandaId = selectedComanda.idComanda
            val loteNumber = loteToProcess.number
            val isAssignedToThisComanda = loteToProcess.number == selectedComanda.numberLoteComanda

            if (cliente == null) return@launch

            // 1. --- LÓGICA DE ANULACIÓN (Desasignación de la comanda) ---
            if (isAssignedToThisComanda) {
                // A) Desasignar de la Comanda (Siempre se hace)
                val comandaSuccess = comandaRepository.updateComandaLoteNumber(comandaId, "")

                var bookingCleared = true
                var message = ""

                if (shouldClearBooking) {
                    // B) Opción 2: Anular Reserva del Lote (Limpia todos los campos de reserva)
                    bookingCleared = loteRepository.updateLoteBooked(loteToProcess.id, null, null, null, null)
                    message = if (bookingCleared) "Lote $loteNumber desasignado y reserva anulada."
                    else "Error al anular reserva."
                } else {
                    // C) Opción 1: Mantener Reserva (Solo desasigna de la Comanda)
                    message = "Lote $loteNumber desasignado (Reserva mantenida)."
                }

                if (comandaSuccess && bookingCleared) {
                    loadLotesForAssignment()
                    onLoteAssignmentSuccess()
                    snackbarHostState.showSnackbar(message)
                } else {
                    snackbarHostState.showSnackbar("Error al desasignar el lote $loteNumber.")
                }
                return@launch
            }

            // 2. --- LÓGICA DE ASIGNACIÓN/CONFIRMACIÓN (Sin cambios) ---
            if (selectedComanda.numberLoteComanda.isNotBlank()) return@launch

            // Asignar y/o confirmar la reserva (Lógica original)
            val loteSuccess = loteRepository.updateLoteBooked(
                loteToProcess.id,
                cliente,
                selectedComanda.dateBookedComanda,
                currentUserEmail,
                null
            )
            val comandaSuccess = comandaRepository.updateComandaLoteNumber(comandaId, loteNumber)

            if (loteSuccess && comandaSuccess) {
                loadLotesForAssignment()
                onLoteAssignmentSuccess()
                snackbarHostState.showSnackbar("Lote $loteNumber asignado a la comanda con éxito.")
            } else {
                snackbarHostState.showSnackbar("Error al asignar lote ($loteSuccess) o comanda ($comandaSuccess).")
            }
        }
    }

    // --- Carga de datos inicial ---
    LaunchedEffect(materialDescription) {
        loadLotesForAssignment()
    }

    // Si la lista está vacía, el pagerState tendrá un 'pageCount' de 0.
    val pagerState = rememberPagerState(initialPage = 0) { lotesDisponibles.size }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Asignar lote a comanda",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = PrimaryColor
        )
        Text(
            text = "Material: $materialDescription",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Cliente: ${selectedComanda.bookedClientComanda?.cliNombre}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor de Altura Fija para Pager/Carga/Vacío
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pagerBoxHeightDp),
            contentAlignment = Alignment.Center
        ) {

            if (isLoading) {
                CircularProgressIndicator(color = PrimaryColor)
            } else if (lotesDisponibles.isEmpty()) {
                Text("No hay lotes disponibles para este material.")
            } else {
                // --- 1. VERTICAL PAGER (El Carrusel) ---
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 60.dp) // Espacio para el efecto 3D
                ) { index ->
                    val lote = lotesDisponibles[index]
                    val certificado = certificados[lote.number]

                    // Cálculo del Page Offset para las animaciones
                    val pageOffset = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction)

                    val scale by animateFloatAsState(
                        targetValue = lerp(0.85f, 1f, 1f - abs(pageOffset)),
                        animationSpec = tween(300)
                    )

                    val alpha by animateFloatAsState(
                        targetValue = lerp(0.55f, 1f, 1f - abs(pageOffset)),
                        animationSpec = tween(300)
                    )

                    val translation by animateFloatAsState(
                        targetValue = pageOffset * 40f,
                        animationSpec = tween(300)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { // Aplicar las animaciones 3D
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                translationY = translation
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        PlanningLoteCard(
                            lote = lote,
                            comanda = selectedComanda,
                            certificado = certificado,
                            snackbarHostState = snackbarHostState,
                            onAssignLote = assignLoteToComanda,
                            onViewBigBags = { /* No implementado aquí */ },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }

                // --- 2. BARRA VERTICAL DE PROGRESO ---
                val totalItems = lotesDisponibles.size
                if (totalItems > 1) {
                    val barWidth = 4.dp
                    val indicatorHeightDp = pagerBoxHeightDp
                    val minThumbHeight = 20.dp

                    val thumbHeight = (indicatorHeightDp / totalItems.toFloat()).coerceAtLeast(minThumbHeight)
                    val currentPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    val normalizedPosition = currentPosition / (totalItems - 1).toFloat()
                    val travelRangePx = with(density) { (indicatorHeightDp - thumbHeight).toPx() }

                    val thumbOffsetPx by animateFloatAsState(
                        targetValue = normalizedPosition * travelRangePx,
                        animationSpec = tween(300)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(barWidth)
                            .align(Alignment.CenterEnd)
                            .padding(vertical = 10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = with(density) { thumbOffsetPx.toDp() })
                                .width(barWidth)
                                .height(thumbHeight)
                                .clip(CircleShape)
                                .background(PrimaryColor)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}