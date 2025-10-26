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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.core.LocalDatabaseUrl
import com.alius.gmrstock.data.FirestoreUrls
import com.alius.gmrstock.data.agruparPorMaterial
import com.alius.gmrstock.data.getClientRepository
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
        // Estado para la base de datos actual
        val databaseUrl = LocalDatabaseUrl.current
        var currentDatabaseUrl by remember { mutableStateOf(databaseUrl) }

        println("HomeScreenContent: databaseUrl inicial = $databaseUrl")
        println("HomeScreenContent: currentDatabaseUrl inicial = $currentDatabaseUrl")

        // Repositorios basados en la DB seleccionada
        val loteRepository = remember(currentDatabaseUrl) {
            println("Creando loteRepository para $currentDatabaseUrl")
            getLoteRepository(currentDatabaseUrl)
        }
        val clientRepository = remember(currentDatabaseUrl) {
            println("Creando clientRepository para $currentDatabaseUrl")
            getClientRepository(currentDatabaseUrl)
        }
        val coroutineScope = rememberCoroutineScope()

        // Función para alternar entre DB1 y DB2
        val swapDatabase: () -> Unit = {
            val previous = currentDatabaseUrl
            currentDatabaseUrl = if (currentDatabaseUrl == FirestoreUrls.DB1_URL) {
                FirestoreUrls.DB2_URL
            } else {
                FirestoreUrls.DB1_URL
            }
            println("Swap DB: de $previous a $currentDatabaseUrl")
            onChangeDatabase()
        }

        val lotes = remember { mutableStateListOf<LoteModel>() }
        var materialGroups by remember { mutableStateOf<List<MaterialGroup>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showLogoutDialog by remember { mutableStateOf(false) }
        var showUnimplementedDialog by remember { mutableStateOf(false) }
        // <-- AGREGADO: Nuevo estado para el diálogo de mantenimiento
        var showMaintenanceDialog by remember { mutableStateOf(false) }

        val sheetStateGroup = rememberModalBottomSheetState()
        var showGroupMaterialBottomSheet by remember { mutableStateOf(false) }
        var selectedGroupForSheet by remember { mutableStateOf<MaterialGroup?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }
        val currentUserEmail = remember(user.email) { user.email.substringBefore("@") }
        val localNavigator = LocalNavigator.currentOrThrow

        LaunchedEffect(currentDatabaseUrl) {
            isLoading = true
            errorMessage = null
            try {
                val loadedLotes = loteRepository.listarLotes("")
                lotes.clear()
                lotes.addAll(loadedLotes)
                materialGroups = agruparPorMaterial(loadedLotes)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
            } finally {
                isLoading = false
            }
        }

        @Composable
        fun ActionButton(
            modifier: Modifier = Modifier,
            icon: ImageVector,
            label: String,
            onClick: () -> Unit
        ) {
            ElevatedCard(
                onClick = onClick,
                modifier = modifier.height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF029083), Color(0xFF00BFA5))
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        @Composable
        fun MaintenanceOption(label: String, icon: ImageVector, onClick: () -> Unit) {
            OutlinedCard(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(
                    width = 1.dp,
                    color = PrimaryColor.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {

                if (showUnimplementedDialog) {
                    AlertDialog(
                        onDismissRequest = { showUnimplementedDialog = false },
                        title = { Text(text = "Funcionalidad en desarrollo", color = PrimaryColor) },
                        text = { Text(text = "Esta funcionalidad aún no está implementada.") },
                        confirmButton = {
                            TextButton(onClick = { showUnimplementedDialog = false }) {
                                Text("Aceptar", color = PrimaryColor)
                            }
                        }
                    )
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text(text = "¿Cerrar sesión?", color = PrimaryColor) },
                        text = { Text(text = "¿Estás seguro de que quieres cerrar la sesión?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showLogoutDialog = false
                                    onLogoutClick()
                                }
                            ) { Text("Aceptar", color = PrimaryColor) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancelar", color = PrimaryColor)
                            }
                        }
                    )
                }

                //DIÁLOGO DE MANTENIMIENTO
                if (showMaintenanceDialog) {
                    AlertDialog(
                        onDismissRequest = { showMaintenanceDialog = false },
                        title = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Mantenimiento de Datos",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor
                                )
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                MaintenanceOption(
                                    label = "Mantenimiento de clientes",
                                    icon = Icons.Default.People,
                                    onClick = {
                                        showMaintenanceDialog = false
                                        // Navegar a CrudClientScreen
                                        localNavigator.push(CrudClientScreen(currentDatabaseUrl))
                                    }
                                )
                                MaintenanceOption(
                                    label = "Mantenimiento de materiales",
                                    icon = Icons.Default.Category,
                                    onClick = {
                                        showMaintenanceDialog = false
                                        showUnimplementedDialog = true
                                    }
                                )
                                MaintenanceOption(
                                    label = "Mantenimiento de reservas",
                                    icon = Icons.Default.Event,
                                    onClick = {
                                        showMaintenanceDialog = false
                                        showUnimplementedDialog = true
                                    }
                                )
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showMaintenanceDialog = false }) {
                                Text("Cerrar", color = PrimaryColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(horizontal = 16.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.SwapHoriz,
                                label = "SWAP",
                                onClick = swapDatabase
                            )
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Tune,
                                label = "Datos",
                                onClick = {
                                    showMaintenanceDialog = true
                                }
                            )
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Search,
                                label = "Buscar",
                                onClick = { showUnimplementedDialog = true }
                            )
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.PowerSettingsNew,
                                label = user.email.substringBefore("@"),
                                onClick = { showLogoutDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
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
                            databaseUrl = currentDatabaseUrl,
                            onRemarkUpdated = { updatedLote ->
                                val index = lotes.indexOfFirst { it.id == updatedLote.id }
                                if (index >= 0) {
                                    lotes[index] = updatedLote
                                    materialGroups = agruparPorMaterial(lotes)
                                }
                            },
                            clientRepository = clientRepository,
                            currentUserEmail = currentUserEmail
                        )
                    }
                }
            }
        }
    }
}