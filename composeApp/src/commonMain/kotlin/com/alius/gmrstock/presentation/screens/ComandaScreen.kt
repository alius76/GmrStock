package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.data.getClientRepository
import com.alius.gmrstock.data.getComandaRepository
import com.alius.gmrstock.data.getMaterialRepository
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.domain.model.Material
import com.alius.gmrstock.ui.components.ClientesSelectedDialogContent
import com.alius.gmrstock.ui.components.ComandaCard
import com.alius.gmrstock.ui.components.InlineCalendarSelector
import com.alius.gmrstock.ui.components.MaterialSelectedDialogContent
import com.alius.gmrstock.ui.components.UniversalDatePickerDialog
import com.alius.gmrstock.ui.components.PlanningAssignmentBottomSheet
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
class ComandaScreen(
    private val databaseUrl: String,
    private val currentUserEmail: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        // --- Repositorios ---
        val clientRepository = remember(databaseUrl) { getClientRepository(databaseUrl) }
        val materialRepository = remember(databaseUrl) { getMaterialRepository(databaseUrl) }
        val comandaRepository = remember(databaseUrl) { getComandaRepository(databaseUrl) }

        // --- Estados principales ---
        var comandasDelDia by remember { mutableStateOf(listOf<Comanda>()) }
        var fechaSeleccionada by remember {
            mutableStateOf(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            )
        }
        var todasLasComandas by remember { mutableStateOf(listOf<Comanda>()) }

        // --- Estados para Añadir Comanda ---
        var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
        var selectedCliente by remember { mutableStateOf<Cliente?>(null) }
        var showClientesDialog by remember { mutableStateOf(false) }

        var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
        var selectedMaterial by remember { mutableStateOf<Material?>(null) }
        var showMaterialDialog by remember { mutableStateOf(false) }

        var showAgregarDialog by remember { mutableStateOf(false) }
        var totalWeightComanda by remember { mutableStateOf("") }
        var remarkComanda by remember { mutableStateOf("") }

        var errorCliente by remember { mutableStateOf(false) }
        var errorDescripcion by remember { mutableStateOf(false) }
        var errorPeso by remember { mutableStateOf(false) }

        var selectedComanda by remember { mutableStateOf<Comanda?>(null) }

        // --- Estados para Reasignar Fecha ---
        var showDatePicker by remember { mutableStateOf(false) }
        var comandaToUpdateDate by remember { mutableStateOf<Comanda?>(null) }

        // --- Estados para ASIGNACIÓN DE LOTE (Nuevo) ---
        var showAssignmentSheet by remember { mutableStateOf(false) }
        var comandaForLote by remember { mutableStateOf<Comanda?>(null) }
        val assignmentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // --- Diálogos y Modificaciones ---
        var showConfirmDeleteDialog by remember { mutableStateOf(false) }
        var showConfirmReassignDateDialog by remember { mutableStateOf(false) }
        var showEditRemarkDialog by remember { mutableStateOf(false) }
        var comandaToModify by remember { mutableStateOf<Comanda?>(null) }
        var newRemark by remember { mutableStateOf("") }
        var newDateSelected by remember { mutableStateOf<LocalDate?>(null) }

        // --- Función Maestra de Refresco ---
        fun refrescarDatos() {
            scope.launch {
                try {
                    val listaDia = comandaRepository.listarComandas(fechaSeleccionada.toString())
                    val listaGlobal = comandaRepository.listarTodasComandas()
                    comandasDelDia = listaDia
                    todasLasComandas = listaGlobal
                } catch (e: Exception) {
                    println("Error al refrescar datos: ${e.message}")
                }
            }
        }

        // --- Cargas iniciales ---
        LaunchedEffect(fechaSeleccionada) {
            refrescarDatos()
        }

        LaunchedEffect(databaseUrl) {
            clients = try {
                clientRepository.getAllClientsOrderedByName().filter { it.cliNombre != "NO OK" }
            } catch (e: Exception) { emptyList() }

            materials = try {
                materialRepository.getAllMaterialsOrderedByName()
            } catch (e: Exception) { emptyList() }
        }

        fun resetFormStates() {
            selectedCliente = null
            selectedMaterial = null
            totalWeightComanda = ""
            remarkComanda = ""
            errorCliente = false
            errorDescripcion = false
            errorPeso = false
        }

        // --- Lógica CRUD ---
        fun guardarComanda() {
            val instantToSave = fechaSeleccionada.atStartOfDayIn(TimeZone.UTC)
            val nuevaComanda = Comanda(
                idComanda = "",
                bookedClientComanda = selectedCliente,
                descriptionLoteComanda = selectedMaterial?.materialNombre ?: "",
                numberLoteComanda = "",
                dateBookedComanda = instantToSave,
                totalWeightComanda = totalWeightComanda,
                remarkComanda = remarkComanda
            )
            scope.launch {
                val exito = comandaRepository.addComanda(nuevaComanda)
                if (exito) {
                    refrescarDatos()
                    resetFormStates()
                }
            }
        }

        fun actualizarObservaciones(comanda: Comanda, text: String) {
            scope.launch {
                comanda.idComanda.takeIf { it.isNotEmpty() }?.let { id ->
                    val exito = comandaRepository.updateComandaRemark(id, text)
                    if (exito) refrescarDatos()
                }
            }
        }

        fun ejecutarEliminar(comanda: Comanda) {
            scope.launch {
                comanda.idComanda.takeIf { it.isNotEmpty() }?.let {
                    val exito = comandaRepository.deleteComanda(it)
                    if (exito) refrescarDatos()
                }
                comandaToModify = null
                showConfirmDeleteDialog = false
                selectedComanda = null
            }
        }

        fun ejecutarReasignacionFinal() {
            val comandaToReassign = comandaToModify
            val targetDate = newDateSelected
            if (comandaToReassign != null && targetDate != null) {
                scope.launch {
                    val newInstant = targetDate.atStartOfDayIn(TimeZone.UTC)
                    val exito = comandaRepository.updateComandaDate(comandaToReassign.idComanda, newInstant)
                    if (exito) {
                        fechaSeleccionada = targetDate
                        refrescarDatos()
                    }
                    comandaToModify = null
                    newDateSelected = null
                    showConfirmReassignDateDialog = false
                }
            }
        }

        fun confirmarEliminar(comanda: Comanda) {
            comandaToModify = comanda
            showConfirmDeleteDialog = true
            selectedComanda = null
        }

        fun confirmarReasignar(comanda: Comanda) {
            comandaToModify = comanda
            comandaToUpdateDate = comanda
            selectedComanda = null
            showDatePicker = true
        }

        fun editarObservaciones(comanda: Comanda) {
            comandaToModify = comanda
            newRemark = comanda.remarkComanda
            showEditRemarkDialog = true
            selectedComanda = null
        }

        // --- UI Principal ---
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text("Gestión de comandas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Seleccione fecha", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        }
                        IconButton(onClick = { navigator.push(ComandasPlanningScreen(databaseUrl, currentUserEmail)) }) {
                            Icon(Icons.Default.ListAlt, contentDescription = "Planning", tint = PrimaryColor, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InlineCalendarSelector(
                        selectedDate = fechaSeleccionada,
                        allComandas = todasLasComandas,
                        onDateSelected = { fechaSeleccionada = it },
                        primaryColor = PrimaryColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val conteo = if (comandasDelDia.isNotEmpty()) " (${comandasDelDia.size})" else ""
                        Text("Comandas$conteo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                        IconButton(onClick = { resetFormStates(); showAgregarDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar", tint = PrimaryColor, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de comandas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    items(comandasDelDia) { comanda ->
                        ComandaCard(
                            comanda = comanda,
                            isSelected = selectedComanda == comanda,
                            onClick = { selectedComanda = if (selectedComanda == comanda) null else comanda },
                            onDelete = { confirmarEliminar(comanda) },
                            onReassign = { confirmarReasignar(comanda) },
                            onEditRemark = { editarObservaciones(comanda) },
                            onAssignLote = { // 🔥 Acción del nuevo botón de Lote
                                comandaForLote = comanda
                                showAssignmentSheet = true
                                selectedComanda = null
                                scope.launch { assignmentSheetState.show() }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // --- Snackbar Host ---
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // --- Dialog Agregar Comanda ---
            if (showAgregarDialog) {
                Dialog(onDismissRequest = {
                    showAgregarDialog = false
                    resetFormStates()
                }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Agregar comanda",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PrimaryColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            // Selección Cliente
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        1.dp,
                                        if (selectedCliente != null) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { showClientesDialog = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    selectedCliente?.cliNombre ?: "Seleccione cliente",
                                    color = if (selectedCliente != null) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            if (errorCliente) Text("Debe seleccionar un cliente válido", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                            // Selección Material
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        1.dp,
                                        if (selectedMaterial != null) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { showMaterialDialog = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    selectedMaterial?.materialNombre ?: "Seleccione material",
                                    color = if (selectedMaterial != null) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            if (errorDescripcion) Text("Debe seleccionar un material", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                            // Peso total
                            OutlinedTextField(
                                value = totalWeightComanda,
                                onValueChange = { input ->
                                    totalWeightComanda = input.filter { it.isDigit() };
                                    errorPeso = false
                                },
                                label = { Text("Peso total (Kg)") },
                                isError = errorPeso,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                            if (errorPeso) Text("Ingrese un número válido mayor a 0", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                            // Observaciones
                            OutlinedTextField(
                                value = remarkComanda,
                                onValueChange = { remarkComanda = it },
                                label = { Text("Observaciones") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 150.dp),
                                singleLine = false,
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )

                            // Botones
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = {
                                    showAgregarDialog = false
                                    resetFormStates()
                                }) { Text("Cancelar", color = PrimaryColor) }

                                TextButton(onClick = {
                                    var valid = true
                                    if (selectedCliente == null) { errorCliente = true; valid = false }
                                    if (selectedMaterial == null) { errorDescripcion = true; valid = false }
                                    val pesoValido = totalWeightComanda.toIntOrNull()?.takeIf { it > 0 } != null
                                    if (!pesoValido) { errorPeso = true; valid = false }
                                    if (!valid) return@TextButton

                                    guardarComanda()
                                    showAgregarDialog = false
                                }) { Text("Guardar", color = PrimaryColor) }
                            }
                        }
                    }
                }
            }

            // --- Dialog Cliente (Sustituido por tu componente optimizado) ---
            if (showClientesDialog) {
                ClientesSelectedDialogContent(
                    clients = clients,
                    currentSelectedClient = selectedCliente,
                    showAllOption = false, // En una nueva comanda NO permitimos "TODOS"
                    onDismiss = {
                        showClientesDialog = false
                    },
                    onConfirm = { clienteSeleccionado ->
                        selectedCliente = clienteSeleccionado
                        errorCliente = false
                        showClientesDialog = false
                    }
                )
            }

            // --- Dialog Material (Sustituido por el nuevo componente) ---
            if (showMaterialDialog) {
                MaterialSelectedDialogContent(
                    materials = materials,
                    currentSelectedMaterial = selectedMaterial,
                    onDismiss = { showMaterialDialog = false },
                    onConfirm = { materialSeleccionado ->
                        selectedMaterial = materialSeleccionado
                        errorDescripcion = false
                        showMaterialDialog = false
                    }
                )
            }

            // --- DatePicker ---
            if (showDatePicker) {
                UniversalDatePickerDialog(
                    initialDate = comandaToUpdateDate?.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date ?: fechaSeleccionada,
                    onDateSelected = { selected ->
                        showDatePicker = false
                        if (comandaToUpdateDate != null) {
                            newDateSelected = selected
                            comandaToModify = comandaToUpdateDate
                            showConfirmReassignDateDialog = true
                        } else {
                            fechaSeleccionada = selected
                        }
                        comandaToUpdateDate = null
                    },
                    onDismiss = {
                        showDatePicker = false
                        comandaToUpdateDate = null
                    },
                    primaryColor = PrimaryColor
                )
            }

            // --- Confirmación Eliminación ---
            if (showConfirmDeleteDialog && comandaToModify != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmDeleteDialog = false; comandaToModify = null },
                    title = { Text("Confirmar anulación") },
                    text = { Text("¿Está seguro de que desea anular la comanda para ${comandaToModify!!.bookedClientComanda?.cliNombre}?") },
                    confirmButton = {
                        TextButton(onClick = { ejecutarEliminar(comandaToModify!!) }) {
                            Text("Anular", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDeleteDialog = false; comandaToModify = null }) {
                            Text("Cancelar", color = PrimaryColor)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // --- Confirmación Reasignación Fecha ---
            if (showConfirmReassignDateDialog && comandaToModify != null && newDateSelected != null) {
                val oldDate = comandaToModify!!.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date ?: fechaSeleccionada
                val formattedNewDate = "${newDateSelected!!.dayOfMonth.toString().padStart(2, '0')}-${newDateSelected!!.monthNumber.toString().padStart(2, '0')}-${newDateSelected!!.year}"
                val formattedOldDate = "${oldDate.dayOfMonth.toString().padStart(2, '0')}-${oldDate.monthNumber.toString().padStart(2, '0')}-${oldDate.year}"

                AlertDialog(
                    onDismissRequest = {
                        showConfirmReassignDateDialog = false
                        comandaToModify = null
                        newDateSelected = null
                    },
                    title = { Text("Confirmar nueva fecha") },
                    text = {
                        Text("¿Desea mover la comanda de ${comandaToModify!!.bookedClientComanda?.cliNombre} del $formattedOldDate al $formattedNewDate?")
                    },
                    confirmButton = {
                        TextButton(onClick = { ejecutarReasignacionFinal() }) {
                            Text("Confirmar", color = PrimaryColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showConfirmReassignDateDialog = false
                            comandaToModify = null
                            newDateSelected = null
                        }) {
                            Text("Cancelar", color = PrimaryColor)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // --- Diálogo de EDICIÓN de Observaciones ---
            if (showEditRemarkDialog && comandaToModify != null) {
                Dialog(onDismissRequest = {
                    showEditRemarkDialog = false
                    comandaToModify = null
                }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Editar observaciones",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PrimaryColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            // --- Campo de Observaciones ---
                            OutlinedTextField(
                                value = newRemark,
                                onValueChange = { newRemark = it },
                                label = { Text("Observaciones") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 150.dp),
                                singleLine = false,
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )

                            // --- Botones ---
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = {
                                    showEditRemarkDialog = false
                                    comandaToModify = null
                                }) { Text("Cancelar", color = PrimaryColor) }

                                TextButton(onClick = {
                                    actualizarObservaciones(comandaToModify!!, newRemark)
                                    showEditRemarkDialog = false
                                    comandaToModify = null
                                }) { Text("Guardar", color = PrimaryColor) }
                            }
                        }
                    }
                }
            }

            // --- BOTTOM SHEET DE ASIGNACIÓN DE LOTES (Nuevo) ---
            if (showAssignmentSheet && comandaForLote != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showAssignmentSheet = false
                        comandaForLote = null
                    },
                    sheetState = assignmentSheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    PlanningAssignmentBottomSheet(
                        selectedComanda = comandaForLote!!,
                        databaseUrl = databaseUrl,
                        currentUserEmail = currentUserEmail,
                        clientRepository = clientRepository,
                        onLoteAssignmentSuccess = {
                            refrescarDatos()
                            scope.launch { assignmentSheetState.hide() }.invokeOnCompletion {
                                if (!assignmentSheetState.isVisible) {
                                    showAssignmentSheet = false
                                    comandaForLote = null
                                }
                            }
                        },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}