package com.alius.gmrstock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.alius.gmrstock.data.ClientRepository
import com.alius.gmrstock.data.LoteRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.CertificadoStatus
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LotesBottomSheetContent(
    loteRepository: LoteRepository,
    clientRepository: ClientRepository,
    databaseUrl: String,
    currentUserEmail: String,
    onViewBigBags: (List<BigBags>) -> Unit,
    onRemarkUpdated: (LoteModel) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var searchText by remember { mutableStateOf("") }
    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // BUSCADOR con debounce y filtro contains
    LaunchedEffect(searchText) {
        isLoading = true
        kotlinx.coroutines.delay(300)

        // *** CAMBIO CLAVE 1: Si no hay texto, no se busca y se deja la lista vacía ***
        if (searchText.isBlank()) {
            lotes = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        // *************************************************************************

        val allLotes = withContext(Dispatchers.IO) {
            try {
                // Aquí deberías optimizar para llamar al repositorio solo con el filtro si es posible.
                // Si no, se obtienen todos y se filtra localmente.
                loteRepository.listarLotes("")
            } catch (e: Exception) {
                emptyList()
            }
        }

        lotes = allLotes.filter { it.number.contains(searchText, ignoreCase = true) }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {

        // 🔍 CAJA DE BÚSQUEDA (MODIFICADA PARA ACEPTAR SOLO NÚMEROS)
        OutlinedTextField(
            value = searchText,
            onValueChange = { newValue ->
                // Filtra la entrada para que solo se guarden dígitos
                searchText = newValue.filter { it.isDigit() }
            },
            placeholder = { Text("Busqueda de lote por número...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            // Muestra el teclado numérico en el dispositivo
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            // *** CAMBIO CLAVE 2A: Muestra mensaje si el campo está vacío ***
            lotes.isEmpty() && searchText.isBlank() -> {
                Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "🔎 Ingrese el número de lote para buscar.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            // ****************************************************************

            // *** CAMBIO CLAVE 2B: Muestra mensaje si no hay resultados para la búsqueda ***
            lotes.isEmpty() && searchText.isNotBlank() -> {
                Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se encontraron lotes para \"$searchText\"",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            // *****************************************************************************

            else -> {
                // Se ejecuta solo si hay lotes (lotes.isNotEmpty())
                val pagerState = rememberPagerState(initialPage = 0) { lotes.size }

                // 🔥 1. CONTENEDOR PAGER (VerticalPager con altura fija)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentAlignment = Alignment.Center
                ) {

                    // 📄 PAGER
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 60.dp)
                    ) { index ->
                        val lote = lotes[index]
                        val cert: Certificado? = null

                        val certColor = when (cert?.status) {
                            CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                            CertificadoStatus.CORRECTO -> PrimaryColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        val pageOffset =
                            pagerState.currentPage - index + pagerState.currentPageOffsetFraction

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
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    translationY = translation
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            LoteCard(
                                lote = lote,
                                certificado = cert,
                                certificadoIconColor = certColor,
                                modifier = Modifier.fillMaxWidth(0.85f),
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                onViewBigBags = onViewBigBags,
                                databaseUrl = databaseUrl,
                                onRemarkUpdated = { updated ->
                                    lotes = lotes.map {
                                        if (it.id == updated.id) updated else it
                                    }
                                    onRemarkUpdated(updated)
                                },
                                clientRepository = clientRepository,
                                currentUserEmail = currentUserEmail
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ●●● 2. DOTS (FUERA del Box para la posición correcta)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(lotes.size) { index ->
                        val isActive = pagerState.currentPage == index

                        val dotSize by animateDpAsState(
                            targetValue = if (isActive) 14.dp else 10.dp,
                            animationSpec = tween(250)
                        )

                        val dotColor by animateColorAsState(
                            targetValue = if (isActive) PrimaryColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            animationSpec = tween(250)
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable {
                                    scope.launch { pagerState.scrollToPage(index) }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}