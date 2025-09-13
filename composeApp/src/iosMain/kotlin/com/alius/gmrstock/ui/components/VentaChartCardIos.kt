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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.ui.theme.PrimaryColor

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun VentaChartCard(
    modifier: Modifier,
    ventaDataList: List<VentaData>
) {
    val data = ventaDataList
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (data.isEmpty()) return@Canvas

            val leftPadding = 50f
            val bottomPadding = 30f
            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding
            val maxWeight = 100_000f // Escala fija
            val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
            val scaleY = chartHeight / maxWeight
            val textStyle = TextStyle(color = Color.Black, fontSize = 12.sp)
            val yLabelXOffset = 2.dp.toPx()

            // Eje Y con etiquetas redondeadas fijas
            val yLabels = listOf(0f, 20_000f, 40_000f, 60_000f, 80_000f, 100_000f)
            val yLabelStrings = listOf("", "20K", "40K", "60K", "80K", "100K")
            yLabels.forEachIndexed { index, value ->
                val y = chartHeight - value * scaleY

                // Línea de cuadrícula
                drawLine(
                    color = Color.LightGray,
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y)
                )

                // Texto del eje Y
                val measuredText = textMeasurer.measure(yLabelStrings[index], textStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = yLabelStrings[index],
                    topLeft = Offset(leftPadding - measuredText.size.width - yLabelXOffset, y - measuredText.size.height / 2),
                    style = textStyle
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
            val measuredFirstDayText = textMeasurer.measure("Día ${data.first().day}", textStyle)
            val baseY = size.height - measuredFirstDayText.size.height - 1.dp.toPx()

            drawText(textMeasurer, "Día ${data.first().day}", Offset(leftPadding, baseY), textStyle)

            val lastDayText = "Día ${data.last().day}"
            val measuredLastDayText = textMeasurer.measure(lastDayText, textStyle)
            drawText(textMeasurer, lastDayText, Offset(leftPadding + chartWidth - measuredLastDayText.size.width, baseY), textStyle)
        }
    }
}
