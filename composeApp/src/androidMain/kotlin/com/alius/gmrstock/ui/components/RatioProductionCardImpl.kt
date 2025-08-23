package com.alius.gmrstock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.ui.theme.PrimaryColor

@Composable
actual fun RatioProductionCard(
    modifier: Modifier,
    ratioDataList: List<RatioData>
) {
    val data = ratioDataList // <-- ahora usa la lista recibida

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        val leftPadding = 50f // espacio para etiquetas Y
        val bottomPadding = 30f // espacio para etiquetas X
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (data.isEmpty()) return@Canvas

            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding
            val maxWeight = 100_000f
            val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
            val scaleY = chartHeight / maxWeight

            // Eje Y abreviado
            val yLabels = listOf(0f, 20_000f, 40_000f, 60_000f, 80_000f, 100_000f)
            val yLabelStrings = listOf("0", "20K", "40K", "60K", "80K", "100K")
            yLabels.forEachIndexed { index, value ->
                val y = chartHeight - value * scaleY
                drawLine(
                    color = Color.LightGray,
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    yLabelStrings[index],
                    0f,
                    y,
                    android.graphics.Paint().apply {
                        textSize = 24f
                        color = android.graphics.Color.BLACK
                    }
                )
            }

            // Línea de datos
            for (i in 0 until data.size - 1) {
                val x1 = leftPadding + i * stepX
                val y1 = chartHeight - data[i].totalWeight * scaleY
                val x2 = leftPadding + (i + 1) * stepX
                val y2 = chartHeight - data[i + 1].totalWeight * scaleY
                drawLine(
                    color = PrimaryColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }

            // Eje X: primer y último día
            val xIndices = listOf(0, data.size - 1)
            xIndices.forEach { i ->
                val x = leftPadding + i * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    "${data[i].day}",
                    x,
                    chartHeight + 20f,
                    android.graphics.Paint().apply {
                        textSize = 24f
                        color = android.graphics.Color.BLACK
                    }
                )
            }
        }
    }
}
