package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.data.getClientRepository
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.ui.theme.ReservedColor
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.BackgroundColor
import com.alius.gmrstock.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class CrudClientScreen(
    private val databaseUrl: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val clientRepo = remember(databaseUrl) { getClientRepository(databaseUrl) }

        var clients by remember { mutableStateOf<List<Pair<String, Cliente>>>(emptyList()) }
        var showEditCreateDialog by remember { mutableStateOf(false) }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }
        var clientToDelete by remember { mutableStateOf<Pair<String, Cliente>?>(null) }
        // 🚀 Nuevo estado de carga
        var loading by remember { mutableStateOf(true) }

        var editingClient by remember { mutableStateOf<Pair<String, Cliente>?>(null) }
        var nameField by remember { mutableStateOf(TextFieldValue("")) }
        var obsField by remember { mutableStateOf(TextFieldValue("")) }

        // 🎨 Definición de colores de TextField para el estado enfocado (SIN CAMBIOS)
        val focusedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor
        )

        // 🎨 NUEVO: Forma redondeada de 12.dp para TextFields y Cards
        val roundedShape12 = RoundedCornerShape(12.dp)

        fun refreshClients() {
            coroutineScope.launch {
                loading = true
                clients = clientRepo.getAllClientsWithIds()
                loading = false
            }
        }

        LaunchedEffect(databaseUrl) { refreshClients() }

        // 🚨 REEMPLAZO DE SCAFFOLD CON BOX (Estructura de pantalla completa)
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {

            // 🚀 Mostrar CircularProgressIndicator si está cargando
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                // --- Contenido principal solo si NO está cargando ---
                Column(modifier = Modifier.fillMaxSize()) {

                    // --- Main Content Area (LazyColumn toma el espacio restante) ---
                    if (clients.isEmpty()) {
                        // Contenido vacío centrado, manteniendo el padding
                        Box(
                            // Este Box ya tiene padding horizontal=16.dp
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Título y Flecha en la parte superior del Box, incluso si la lista está vacía
                            Column(modifier = Modifier.fillMaxWidth()) {

                                // 1. Botón de Atrás (Top Left - SIN CAMBIOS)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    IconButton(onClick = { navigator.pop() }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                                    }
                                }

                                // 2. Título Principal: "Gestión de clientes" (SIN CAMBIOS)
                                Text(
                                    text = "Gestión de clientes",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.secondary,
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            // Mensaje central si está vacío
                            Box(
                                modifier = Modifier.fillMaxSize().padding(top = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay clientes registrados.", color = TextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            // MEJORA: Aumentar espaciado entre tarjetas
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            // --- Área de Títulos Combinada (Encabezado del LazyColumn - SIN CAMBIOS) ---
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {

                                    // 1. Botón de Atrás (Top Left - SIN CAMBIOS)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        IconButton(onClick = { navigator.pop() }) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                                        }
                                    }

                                    // 2. Título Principal: "Gestión de clientes" (SIN CAMBIOS)
                                    Text(
                                        text = "Gestión de clientes",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary,
                                    )

                                    // 3. Subtítulo: "Clientes Registrados" (SIN CAMBIOS)
                                    Text(
                                        text = "Clientes Registrados",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )
                                }
                            }

                            // --- Cards de Clientes (MEJORADO) ---
                            items(clients) { (documentId, cliente) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    // MEJORA: Esquinas redondeadas a 12.dp
                                    shape = roundedShape12,
                                    colors = CardDefaults.cardColors(
                                        // MEJORA: Usar color de fondo estándar de tarjeta para contraste
                                        containerColor = Color.White
                                    ),
                                    // MEJORA: Reducir un poco la elevación para un look más plano
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Información del Cliente
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                cliente.cliNombre,
                                                // MEJORA: Font más grande y negrita para destacar
                                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryColor, // Usar PrimaryColor
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (cliente.cliObservaciones.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Obs: ${cliente.cliObservaciones}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.DarkGray, // Un gris más oscuro que TextSecondary
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // 2. Botones de Acción (Derecha - SIN CAMBIOS EN COLORES, solo espaciado)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp), // Aumentar espacio entre iconos
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {
                                                editingClient = documentId to cliente
                                                nameField = TextFieldValue(cliente.cliNombre)
                                                obsField = TextFieldValue(cliente.cliObservaciones)
                                                showEditCreateDialog = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PrimaryColor)
                                            }
                                            IconButton(onClick = {
                                                clientToDelete = documentId to cliente
                                                showDeleteConfirmDialog = true
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ReservedColor)
                                            }
                                        }
                                    }
                                }
                            }
                            // Espacio al final de la lista para el FAB
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                } // Fin Column (Content)

                // 🚨 Floating Action Button (SIN CAMBIOS)
                FloatingActionButton(
                    onClick = {
                        editingClient = null
                        nameField = TextFieldValue("")
                        obsField = TextFieldValue("")
                        showEditCreateDialog = true
                    },
                    containerColor = PrimaryColor,
                    // MEJORA: Asegurar que la forma sea circular (50% de radio)
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo cliente", tint = Color.White)
                }
            } // Fin del bloque 'else' (no loading)

            // 🔹 Dialogo Crear/Editar (MEJORADO)
            if (showEditCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showEditCreateDialog = false },
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                if (editingClient == null) "Nuevo cliente" else "Editar cliente",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // Aumentar espacio
                            // MEJORA: Aplicar forma redondeada de 12.dp
                            OutlinedTextField(
                                value = nameField,
                                onValueChange = { nameField = it },
                                label = { Text("Nombre del cliente") },
                                singleLine = true,
                                shape = roundedShape12,
                                colors = focusedTextFieldColors
                            )
                            // MEJORA: Aplicar forma redondeada de 12.dp
                            OutlinedTextField(
                                value = obsField,
                                onValueChange = { obsField = it },
                                label = { Text("Observaciones") },
                                maxLines = 3,
                                shape = roundedShape12,
                                colors = focusedTextFieldColors
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                val nuevo = Cliente(
                                    cliNombre = nameField.text.trim(),
                                    cliObservaciones = obsField.text.trim()
                                )
                                if (editingClient == null) {
                                    clientRepo.addClient(nuevo)
                                } else {
                                    clientRepo.updateClient(editingClient!!.first, nuevo)
                                }
                                showEditCreateDialog = false
                                refreshClients()
                            }
                        }) { Text("Guardar", fontWeight = FontWeight.SemiBold, color = PrimaryColor) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditCreateDialog = false }) { Text("Cancelar", fontWeight = FontWeight.SemiBold, color = PrimaryColor) }
                    }
                )
            }

            // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN (MEJORADO) ---
            if (showDeleteConfirmDialog && clientToDelete != null) {
                val (documentId, cliente) = clientToDelete!!
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Advertencia", tint = ReservedColor) },
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                "Confirmar eliminación",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                    },
                    text = {
                        Text("¿Está seguro de que desea eliminar al cliente ${cliente.cliNombre}?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                clientRepo.deleteClient(documentId)
                                showDeleteConfirmDialog = false
                                clientToDelete = null
                                refreshClients()
                            }
                        }) {
                            // MEJORA: Usar ReservedColor para la acción destructiva
                            Text("Eliminar", color = ReservedColor, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteConfirmDialog = false
                            clientToDelete = null
                        }) {
                            Text("Cancelar", color = PrimaryColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        } // Fin Box de pantalla completa
    }
}