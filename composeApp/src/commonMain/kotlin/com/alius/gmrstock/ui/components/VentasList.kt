package com.alius.gmrstock.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.domain.model.Venta

@Composable
fun VentasList(
    ventas: List<Venta>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 8.dp,
            bottom = 16.dp // espacio extra abajo
        )
    ) {
        items(ventas) { venta ->
            VentaItem(venta)
        }
    }
}


