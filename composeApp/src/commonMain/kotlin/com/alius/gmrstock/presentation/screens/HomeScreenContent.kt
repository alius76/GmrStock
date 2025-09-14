package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.core.LocalDatabaseUrl
import com.alius.gmrstock.data.agruparPorMaterial
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.ui.components.GroupMaterialBottomSheetContent
import com.alius.gmrstock.ui.components.MaterialGroupCard
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class HomeScreenContent(
    private val user: User,
    private val onChangeDatabase: () -> Unit,
    private val onLogoutClick: () -> Unit = {}
) : Screen {

    @Composable
    override fun Content() {
        val databaseUrl = LocalDatabaseUrl.current
        val loteRepository = remember(databaseUrl) { getLoteRepository(databaseUrl) }
        val coroutineScope = rememberCoroutineScope()

        var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
        var materialGroups by remember { mutableStateOf<List<MaterialGroup>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val sheetStateGroup = rememberModalBottomSheetState()
        var showGroupMaterialBottomSheet by remember { mutableStateOf(false) }
        var selectedGroupForSheet by remember { mutableStateOf<MaterialGroup?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        // Cargar datos cada vez que cambie databaseUrl
        LaunchedEffect(databaseUrl) {
            isLoading = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    lotes = loteRepository.listarLotes("")
                    materialGroups = agruparPorMaterial(lotes)
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Error desconocido"
                } finally {
                    isLoading = false
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.height(48.dp) // altura más compacta
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = user.email,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // menos separación
                    IconButton(
                        onClick = { onLogoutClick() },
                        modifier = Modifier.size(32.dp) // icono más pequeño
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Cerrar sesión",
                            tint = PrimaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    materialGroups.isEmpty() -> {
                        Text(
                            text = "No se encontraron materiales.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            // BLOQUE SUPERIOR: título y subtítulo
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Spacer(modifier = Modifier.height(2.dp)) // casi pegado arriba

                                    Text(
                                        text = "Materiales en stock",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Número de materiales: ${materialGroups.size}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }
                            }

                            items(materialGroups) { group ->
                                MaterialGroupCard(group = group) { clickedGroup ->
                                    selectedGroupForSheet = clickedGroup
                                    showGroupMaterialBottomSheet = true
                                    coroutineScope.launch { sheetStateGroup.show() }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showGroupMaterialBottomSheet && selectedGroupForSheet != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetStateGroup.hide()
                        showGroupMaterialBottomSheet = false
                        selectedGroupForSheet = null
                    }
                },
                sheetState = sheetStateGroup,
                modifier = Modifier.fillMaxHeight(0.5f)
            ) {
                GroupMaterialBottomSheetContent(
                    loteNumbers = selectedGroupForSheet!!.loteNumbers,
                    onLoteClick = { lote -> println("Lote clickeado: ${lote.number}") },
                    onDismissRequest = {
                        coroutineScope.launch {
                            sheetStateGroup.hide()
                            showGroupMaterialBottomSheet = false
                            selectedGroupForSheet = null
                        }
                    },
                    snackbarHostState = snackbarHostState,
                    onViewBigBags = { bigBagsList: List<BigBags> ->
                        println("Mostrando ${bigBagsList.size} BigBags")
                    },
                    databaseUrl = databaseUrl
                )
            }
        }
    }
}
