package com.alius.gmrstock.ui.components

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
import com.alius.gmrstock.domain.model.VentaBigbag

@Composable
fun VentaBigBagsDialogContent(bigBags: List<VentaBigbag>) {
    val primaryColor = Color(0xFF029083)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (bigBags.isEmpty()) {
            Text("No hay big bags para esta venta.")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Número", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                    Text("Peso (Kg)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(bigBags) { bigBag ->
                    VentaBigBagListItem(bigBag, primaryColor)
                }
            }
        }
    }
}

@Composable
fun VentaBigBagListItem(bigBag: VentaBigbag, primaryColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bigBag.ventaBbNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = primaryColor,
                modifier = Modifier.weight(0.5f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = "${bigBag.ventaBbWeight} Kg",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.weight(0.5f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}
