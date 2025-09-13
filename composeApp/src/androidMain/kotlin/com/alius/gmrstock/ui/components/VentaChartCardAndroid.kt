package com.alius.gmrstock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
actual fun VentaChartCard(
    modifier: Modifier,
    ventaDataList: List<VentaData>
) {
    val data = ventaDataList

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        val leftPadding = 50f
        val bottomPadding = 30f

        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            if (data.isEmpty()) return@Canvas

            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding

            // Escala fija de Y
            val yLabels = listOf(0f, 20_000f, 40_000f, 60_000f, 80_000f, 100_000f)
            val yLabelStrings = listOf("", "20K", "40K", "60K", "80K", "100K")
            val scaleY = chartHeight / 100_000f

            // Dibujo de líneas horizontales y etiquetas Y
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
            val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
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
            val paint = android.graphics.Paint().apply {
                textSize = 24f
                color = android.graphics.Color.BLACK
            }
            val yPos = chartHeight + 40f

            // Etiqueta primer día
            drawContext.canvas.nativeCanvas.drawText(
                "Día ${data.first().day}",
                leftPadding,
                yPos,
                paint
            )

            // Etiqueta último día
            val lastDayText = "Día ${data.last().day}"
            val textWidth = paint.measureText(lastDayText)
            val xOffset = 20f
            drawContext.canvas.nativeCanvas.drawText(
                lastDayText,
                leftPadding + chartWidth - textWidth - xOffset,
                yPos,
                paint
            )
        }
    }
}
