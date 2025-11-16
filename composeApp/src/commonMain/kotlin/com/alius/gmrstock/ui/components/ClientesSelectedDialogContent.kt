package com.alius.gmrstock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.ui.theme.TextSecondary

@Composable
fun ClientesSelectedDialogContent(
    clientes: List<Cliente>,
    selectedCliente: Cliente?,
    onClienteSelected: (Cliente) -> Unit,
    onDismiss: () -> Unit
) {
    val primaryColor = PrimaryColor

    Column(modifier = Modifier.fillMaxWidth()) {
        if (clientes.isEmpty()) {
            Text(
                text = "No hay clientes disponibles.",
                modifier = Modifier.padding(16.dp),
                color = primaryColor.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(clientes) { cliente ->
                    val isSelected = selectedCliente == cliente

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, primaryColor),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) primaryColor else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClienteSelected(cliente) }
                            .padding(horizontal = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = cliente.cliNombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cliente.cliObservaciones,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                else TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
