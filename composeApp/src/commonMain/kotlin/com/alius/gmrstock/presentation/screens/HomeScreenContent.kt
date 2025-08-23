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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
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
    private val onLogoutClick: () -> Unit = {} // ✅ agregado
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
            topBar = {
                // AppBar personalizada con icono + email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Primero el email
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.width(0.dp)) // Pequeño espacio entre email y icono

                    // Luego el icono de logout
                    IconButton(onClick = { onLogoutClick() }) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Cerrar sesión",
                            tint = PrimaryColor
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(paddingValues)
            ) {
                Spacer(Modifier.height(4.dp))

                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    errorMessage != null -> {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    materialGroups.isEmpty() -> {
                        Text(
                            text = "No se encontraron materiales.",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    else -> {
                        Column {
                            Text(
                                text = "Materiales en stock",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 0.dp, bottom = 18.dp)
                            )

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
                    },
                    databaseUrl = databaseUrl
                )
            }
        }
    }
}
