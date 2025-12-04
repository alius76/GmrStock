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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ListAlt // 🔑 NUEVA IMPORTACIÓN
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
import com.alius.gmrstock.ui.components.ComandaCard
import com.alius.gmrstock.ui.components.UniversalDatePickerDialog
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
class ComandaScreen(private val databaseUrl: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

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

        // ... (el resto de los estados se mantienen iguales) ...
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

        var showDatePicker by remember { mutableStateOf(false) }
        var comandaToUpdateDate by remember { mutableStateOf<Comanda?>(null) }

        // --- Cargar clientes ---
        LaunchedEffect(databaseUrl) {
            clients = try {
                clientRepository.getAllClientsOrderedByName().filter { it.cliNombre != "NO OK" }
            } catch (e: Exception) {
                emptyList()
            }
        }

        // --- Cargar materiales ---
        LaunchedEffect(databaseUrl) {
            materials = try {
                materialRepository.getAllMaterialsOrderedByName()
            } catch (e: Exception) {
                emptyList()
            }
        }

        // 🌟 FUNCIÓN: Restablecer todos los estados del formulario de nueva comanda
        fun resetFormStates() {
            selectedCliente = null
            selectedMaterial = null
            totalWeightComanda = ""
            remarkComanda = ""
            errorCliente = false
            errorDescripcion = false
            errorPeso = false
        }

        // --- Funciones principales ---
        fun loadComandasPorFecha(fecha: LocalDate) {
            scope.launch {
                try {
                    comandasDelDia = comandaRepository.listarComandas(fecha.toString())
                } catch (_: Exception) {
                    comandasDelDia = emptyList()
                }
            }
        }

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
                    loadComandasPorFecha(fechaSeleccionada)
                    resetFormStates()
                }
            }
        }

        fun eliminarComanda(comanda: Comanda) {
            scope.launch {
                comanda.idComanda.takeIf { it.isNotEmpty() }?.let {
                    val exito = comandaRepository.deleteComanda(it)
                    if (exito) loadComandasPorFecha(fechaSeleccionada)
                }
            }
        }

        suspend fun updateComandaDate(comanda: Comanda, newDate: LocalDate) {
            val newInstant = newDate.atStartOfDayIn(TimeZone.UTC)
            comanda.idComanda.takeIf { it.isNotEmpty() }?.let { id ->
                val exito = comandaRepository.updateComandaDate(id, newInstant)
                if (exito) loadComandasPorFecha(fechaSeleccionada)
            }
        }

        fun reasignarComanda(comanda: Comanda) {
            comandaToUpdateDate = comanda
            showDatePicker = true
        }

        // --- UI Principal ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // --- Header con flecha, planning, título y fecha ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Flecha de volver
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                        }

                        // 2. Título y subtítulo (MODIFICADO para ocupar espacio y empujar)
                        Column(
                            modifier = Modifier
                                .weight(1f) // ⬅️ HACEMOS QUE ESTE ELEMENTO OCUPE EL ESPACIO CENTRAL
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                "Gestión de comandas",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Seleccione fecha",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // 3. Botón para ir a Planning (MOVEMOS AL FINAL)
                        IconButton(
                            onClick = { navigator.push(ComandasPlanningScreen(databaseUrl)) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.ListAlt, contentDescription = "Planning Reservas", tint = PrimaryColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ... (El botón de selección de fecha del calendario sigue aquí) ...
                    OutlinedButton(
                        onClick = { comandaToUpdateDate = null; showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Calendario", tint = PrimaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${fechaSeleccionada.dayOfMonth.toString().padStart(2, '0')}-${fechaSeleccionada.monthNumber.toString().padStart(2, '0')}-${fechaSeleccionada.year}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Comandas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                        IconButton(
                            onClick = { resetFormStates(); showAgregarDialog = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar Comanda", tint = PrimaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Lista de comandas ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(comandasDelDia) { comanda ->
                        ComandaCard(
                            comanda = comanda,
                            isSelected = selectedComanda == comanda,
                            onClick = { selectedComanda = if (selectedComanda == comanda) null else comanda },
                            onDelete = { eliminarComanda(comanda); selectedComanda = null },
                            onReassign = { reasignarComanda(comanda); selectedComanda = null }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ... (Resto de los diálogos y DatePicker) ...

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
                                "Agregar Comanda",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PrimaryColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            // --- Selección Cliente ---
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

                            // --- Selección Material ---
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

                            // --- Peso total ---
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

                            // --- Observaciones ---
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

                            // --- Botones ---
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

            // --- Dialog Cliente ---
            if (showClientesDialog) {
                var tempCliente by remember { mutableStateOf(selectedCliente) }
                AlertDialog(
                    onDismissRequest = { showClientesDialog = false },
                    title = { Text("Seleccione un cliente", fontWeight = FontWeight.Bold, color = PrimaryColor) },
                    text = {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(clients) { cliente ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tempCliente = cliente }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = tempCliente == cliente, onClick = { tempCliente = cliente }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(cliente.cliNombre)
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = {
                        selectedCliente = tempCliente
                        errorCliente = false
                        showClientesDialog = false
                    }) { Text("Aceptar", color = PrimaryColor) } },
                    dismissButton = { TextButton(onClick = { showClientesDialog = false }) { Text("Cancelar", color = PrimaryColor) } },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // --- Dialog Material ---
            if (showMaterialDialog) {
                var tempMaterial by remember { mutableStateOf(selectedMaterial) }
                AlertDialog(
                    onDismissRequest = { showMaterialDialog = false },
                    title = { Text("Seleccione un material", fontWeight = FontWeight.Bold, color = PrimaryColor) },
                    text = {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(materials) { material ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tempMaterial = material }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = tempMaterial == material, onClick = { tempMaterial = material }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(material.materialNombre)
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = {
                        selectedMaterial = tempMaterial
                        errorDescripcion = false
                        showMaterialDialog = false
                    }) { Text("Aceptar", color = PrimaryColor) } },
                    dismissButton = { TextButton(onClick = { showMaterialDialog = false }) { Text("Cancelar", color = PrimaryColor) } },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // --- DatePicker ---
            if (showDatePicker) {
                val datePickerScope = rememberCoroutineScope()

                UniversalDatePickerDialog(
                    initialDate = comandaToUpdateDate?.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date ?: fechaSeleccionada,

                    onDateSelected = { selected ->
                        showDatePicker = false

                        val comandaToReassign = comandaToUpdateDate
                        comandaToUpdateDate = null

                        if (comandaToReassign != null) {
                            datePickerScope.launch {
                                updateComandaDate(comandaToReassign, selected)
                            }
                        } else {
                            fechaSeleccionada = selected
                            loadComandasPorFecha(selected)
                        }
                    },
                    onDismiss = {
                        showDatePicker = false
                        comandaToUpdateDate = null
                    },
                    primaryColor = PrimaryColor
                )
            }

            // --- Cargar comandas iniciales ---
            LaunchedEffect(fechaSeleccionada) { loadComandasPorFecha(fechaSeleccionada) }
        }
    }
}