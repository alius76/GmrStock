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

        // 🎨 Definición de colores de TextField para el estado enfocado
        val focusedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor
        )

        fun refreshClients() {
            coroutineScope.launch {
                loading = true // Inicia la carga
                clients = clientRepo.getAllClientsWithIds()
                loading = false // Finaliza la carga
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
                    CircularProgressIndicator(
                        color = PrimaryColor // Usando el color definido localmente
                    )
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

                                // 1. Botón de Atrás (Top Left - en el espacio de 50.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp), // Altura fija para compensar la barra de estado
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start // Alineado a la izquierda
                                ) {
                                    // El IconButton tiene padding interno que lo mueve ligeramente,
                                    // pero está alineado con el inicio de la Row (borde del Box)
                                    IconButton(onClick = { navigator.pop() }) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Atrás",
                                            tint = PrimaryColor,
                                        )
                                    }
                                }

                                // 2. Título Principal: "Gestión de clientes" (alineado a la izquierda)
                                Text(
                                    text = "Gestión de clientes",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 26.sp, // Tamaño y peso del título principal
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.secondary,
                                    // ✅ QUITADO padding(horizontal = 16.dp) para alinear con el borde del Box/Card
                                )

                                Spacer(modifier = Modifier.height(20.dp)) // Espacio después del encabezado
                            }

                            // Mensaje central si está vacío
                            Box(
                                modifier = Modifier.fillMaxSize().padding(top = 100.dp), // Ajuste para que no colisione con el título
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay clientes registrados.", color = TextSecondary)
                            }

                        }
                    } else {
                        LazyColumn(
                            // Este LazyColumn ya tiene padding horizontal=16.dp
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            // --- Área de Títulos Combinada (Alineada a la referencia) ---
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {

                                    // 1. Botón de Atrás (Top Left - en el espacio de 50.dp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp), // Altura fija para compensar la barra de estado
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start // Alineado a la izquierda
                                    ) {
                                        // Alineado con el borde del LazyColumn
                                        IconButton(onClick = { navigator.pop() }) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "Atrás",
                                                tint = PrimaryColor,
                                            )
                                        }
                                    }

                                    // 2. Título Principal: "Gestión de clientes"
                                    Text(
                                        text = "Gestión de clientes",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 26.sp, // Tamaño y peso del título principal
                                            fontWeight = FontWeight.Bold
                                        ),
                                        // ✅ CAMBIADO A MaterialTheme.colorScheme.secondary
                                        color = MaterialTheme.colorScheme.secondary,
                                        // ✅ QUITADO padding(horizontal = 16.dp)
                                    )

                                    // 3. Subtítulo: "Clientes Registrados" (Antiguo título de la lista)
                                    Text(
                                        text = "Clientes Registrados",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color.Gray, // Color secundario para el subtítulo
                                        // ✅ QUITADO padding(start = 16.dp, end = 16.dp)
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )
                                }
                            }

                            // --- Cards de Clientes con la nueva estética ---
                            items(clients) { (documentId, cliente) ->
                                Card(
                                    // Alineada con el borde del LazyColumn (16.dp)
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    // 🎨 USO DE BACKGROUNDCOLOR
                                    colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                // 🎨 USO DE PRIMARYCOLOR
                                                color = PrimaryColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (cliente.cliObservaciones.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Obs: ${cliente.cliObservaciones}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    // 🎨 USO DE TEXTSECONDARY
                                                    color = TextSecondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // 2. Botones de Acción (Derecha)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {
                                                editingClient = documentId to cliente
                                                nameField = TextFieldValue(cliente.cliNombre)
                                                obsField = TextFieldValue(cliente.cliObservaciones)
                                                showEditCreateDialog = true
                                            }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    // 🎨 USO DE PRIMARYCOLOR
                                                    tint = PrimaryColor
                                                )
                                            }
                                            IconButton(onClick = {
                                                clientToDelete = documentId to cliente
                                                showDeleteConfirmDialog = true
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    // 🎨 USO DE RESERVEDCOLOR
                                                    tint = ReservedColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // Fin Column (Arrow + Content)

                // 🚨 Floating Action Button (Manualmente posicionado)
                FloatingActionButton(
                    onClick = {
                        editingClient = null
                        nameField = TextFieldValue("")
                        obsField = TextFieldValue("")
                        showEditCreateDialog = true
                    },
                    // 🎨 USO DE PRIMARYCOLOR
                    containerColor = PrimaryColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // Posicionamiento manual
                        .padding(24.dp) // Padding exterior
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo cliente", tint = Color.White)
                }
            } // Fin del bloque 'else' (no loading)

            // 🔹 Dialogo Crear/Editar
            if (showEditCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showEditCreateDialog = false },
                    title = {
                        // ✅ Título centrado
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                if (editingClient == null) "Nuevo cliente" else "Editar cliente",
                                fontWeight = FontWeight.Bold,
                                // 🎨 USO DE PRIMARYCOLOR
                                color = PrimaryColor
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // ✅ TextField con color enfocado PrimaryColor
                            OutlinedTextField(
                                value = nameField,
                                onValueChange = { nameField = it },
                                label = { Text("Nombre del cliente") },
                                singleLine = true,
                                colors = focusedTextFieldColors
                            )
                            // ✅ TextField con color enfocado PrimaryColor
                            OutlinedTextField(
                                value = obsField,
                                onValueChange = { obsField = it },
                                label = { Text("Observaciones") },
                                maxLines = 3,
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

            // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ---
            if (showDeleteConfirmDialog && clientToDelete != null) {
                val (documentId, cliente) = clientToDelete!!
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    // 🎨 USO DE RESERVEDCOLOR
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Advertencia", tint = ReservedColor) },
                    title = {
                        // ✅ Título centrado
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                "Confirmar eliminación",
                                fontWeight = FontWeight.Bold,
                                // 🎨 USO DE PRIMARYCOLOR
                                color = PrimaryColor
                            )
                        }
                    },
                    text = {
                        // El diálogo de confirmación no tiene TextFields, solo el mensaje de texto.
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
                            // 🎨 USO DE RESERVEDCOLOR
                            Text("Eliminar", color = PrimaryColor, fontWeight = FontWeight.SemiBold)
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
