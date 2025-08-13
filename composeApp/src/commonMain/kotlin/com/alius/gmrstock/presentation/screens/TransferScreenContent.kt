package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.data.getVentaRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.ui.components.VentasList

@Composable
fun TransferScreenContent(user: User, databaseUrl: String) {
    // Obtenemos el repositorio de ventas con la base de datos proporcionada
    val ventaRepository = remember(databaseUrl) { getVentaRepository(databaseUrl) }

    var ventas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(databaseUrl) {
        ventas = ventaRepository.mostrarTodasLasVentas()
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        VentasList(ventas)
    }
}
