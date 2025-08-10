package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.data.agruparPorMaterial
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.ui.components.GroupMaterialBottomSheetContent
import com.alius.gmrstock.ui.components.MaterialGroupCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class HomeScreenContent(private val user: User) : Screen {

    @Composable
    override fun Content() {
        val loteRepository = remember { getLoteRepository() }
        val coroutineScope = rememberCoroutineScope()

        var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
        var materialGroups by remember { mutableStateOf<List<MaterialGroup>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Estado para el BottomSheet Material3 (sin parámetros)
        val sheetStateGroup = rememberModalBottomSheetState()

        var showGroupMaterialBottomSheet by remember { mutableStateOf(false) }
        var selectedGroupForSheet by remember { mutableStateOf<MaterialGroup?>(null) }

        // Snackbar host state para mostrar mensajes
        val snackbarHostState = remember { SnackbarHostState() }

        // Cargar datos
        LaunchedEffect(Unit) {
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
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
            ) {
                Text(text = "¡Bienvenido, ${user.email}!", fontSize = 24.sp)

                Spacer(Modifier.height(24.dp))

                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    errorMessage != null -> {
                        Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                    }
                    materialGroups.isEmpty() -> {
                        Text(text = "No se encontraron materiales.")
                    }
                    else -> {
                        LazyColumn {
                            items(materialGroups) { group ->
                                MaterialGroupCard(group = group) { clickedGroup ->
                                    selectedGroupForSheet = clickedGroup
                                    showGroupMaterialBottomSheet = true
                                    coroutineScope.launch {
                                        sheetStateGroup.show()
                                    }
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
                modifier = Modifier.fillMaxWidth()
            ) {
                GroupMaterialBottomSheetContent(
                    loteNumbers = selectedGroupForSheet!!.loteNumbers,
                    onLoteClick = { lote ->
                        println("Lote clickeado: ${lote.number}")
                    },
                    onDismissRequest = {
                        coroutineScope.launch {
                            sheetStateGroup.hide()
                            showGroupMaterialBottomSheet = false
                            selectedGroupForSheet = null
                        }
                    },
                    snackbarHostState = snackbarHostState,
                    onGeneratePdf = { lote ->
                        println("Generando PDF para lote ${lote.number}")
                    },
                    onViewBigBags = { bigBagsList: List<BigBags> ->
                        println("Mostrando ${bigBagsList.size} BigBags")
                    }
                )
            }
        }
    }
}
