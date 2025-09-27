package com.alius.gmrstock.presentation.screens

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
import com.alius.gmrstock.core.LocalDatabaseUrl
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
        val databaseUrl = LocalDatabaseUrl.current
        val loteRepository = remember(databaseUrl) { getLoteRepository(databaseUrl) }
        val clientRepository = remember(databaseUrl) { getClientRepository(databaseUrl) }
        val coroutineScope = rememberCoroutineScope()

        val lotes = remember { mutableStateListOf<LoteModel>() }
        var materialGroups by remember { mutableStateOf<List<MaterialGroup>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showLogoutDialog by remember { mutableStateOf(false) }

        val sheetStateGroup = rememberModalBottomSheetState()
        var showGroupMaterialBottomSheet by remember { mutableStateOf(false) }
        var selectedGroupForSheet by remember { mutableStateOf<MaterialGroup?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(databaseUrl) {
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

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {

                // Diálogo logout
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
                    // 🔹 Column principal para botones + lista, pegado arriba
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(horizontal = 16.dp)
                    ) {

                        // Botones de acción
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
                                onClick = onChangeDatabase
                            )
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Bookmark,
                                label = "Reservar",
                                onClick = { /* Acción reservar */ }
                            )
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Search,
                                label = "Consultas",
                                onClick = { /* Acción consultas */ }
                            )
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.PowerSettingsNew,
                                label = user.email.substringBefore("@"),
                                onClick = { showLogoutDialog = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // LazyColumn solo para materiales
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

                // BottomSheet
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
                            databaseUrl = databaseUrl,
                            onRemarkUpdated = { updatedLote ->
                                val index = lotes.indexOfFirst { it.id == updatedLote.id }
                                if (index >= 0) {
                                    lotes[index] = updatedLote
                                    materialGroups = agruparPorMaterial(lotes)
                                }
                            },
                            clientRepository = clientRepository
                        )
                    }
                }
            }
        }
    }
}
