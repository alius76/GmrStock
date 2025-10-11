package com.alius.gmrstock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.core.utils.formatWeight

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun RatioProductionCard(
    modifier: Modifier,
    ratioDataList: List<RatioData>,
    isAnnual: Boolean
) {
    val data = ratioDataList
    if (data.isEmpty()) return

    val maxWeight = if (isAnnual) 1_000_000f else 100_000f
    val yLabels = if (isAnnual)
        listOf(0f, 200_000f, 400_000f, 600_000f, 800_000f, 1_000_000f)
    else
        listOf(0f, 20_000f, 40_000f, 60_000f, 80_000f, 100_000f)

    val yLabelStrings = yLabels.map {
        if (it >= 1_000_000f) "${(it / 1_000_000).toInt()}M"
        else "${(it / 1000).toInt()}K"
    }

    // ✅ Se resetea cuando cambia "data"
    var selectedIndex by remember(data) { mutableStateOf<Int?>(null) }
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
                .pointerInput(data) {
                    detectTapGestures { tapOffset ->
                        val leftPadding = 60f
                        val chartWidth = size.width - leftPadding
                        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
                        val idx = ((tapOffset.x - leftPadding + stepX / 2) / stepX).toInt()
                        selectedIndex = idx.coerceIn(0, data.lastIndex)
                    }
                }
        ) {
            val leftPadding = 60f
            val bottomPadding = 40f
            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding
            val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
            val scaleY = chartHeight / maxWeight
            val textStyle = TextStyle(color = Color.Black, fontSize = 12.sp)

            // --- Eje Y ---
            yLabels.forEachIndexed { index, value ->
                val y = chartHeight - value * scaleY
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = yLabelStrings[index],
                    topLeft = Offset(
                        leftPadding - textMeasurer.measure(yLabelStrings[index], textStyle).size.width - 4.dp.toPx(),
                        y - 6.dp.toPx()
                    ),
                    style = textStyle
                )
            }

            // --- Área bajo la curva ---
            val areaPath = Path().apply {
                moveTo(leftPadding, chartHeight - data.first().totalWeight * scaleY)
                data.forEachIndexed { index, d ->
                    val x = leftPadding + index * stepX
                    val y = chartHeight - d.totalWeight * scaleY
                    lineTo(x, y)
                }
                lineTo(leftPadding + chartWidth, chartHeight)
                lineTo(leftPadding, chartHeight)
                close()
            }
            drawPath(areaPath, brush = Brush.verticalGradient(listOf(PrimaryColor.copy(alpha = 0.3f), Color.Transparent)))

            // --- Línea principal ---
            for (i in 0 until data.size - 1) {
                val x1 = leftPadding + i * stepX
                val y1 = chartHeight - data[i].totalWeight * scaleY
                val x2 = leftPadding + (i + 1) * stepX
                val y2 = chartHeight - data[i + 1].totalWeight * scaleY
                drawLine(
                    brush = Brush.linearGradient(listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.7f))),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }

            // --- Puntos ---
            data.forEachIndexed { index, d ->
                val x = leftPadding + index * stepX
                val y = chartHeight - d.totalWeight * scaleY
                drawCircle(color = PrimaryColor, radius = 6f, center = Offset(x, y))
            }

            // --- Línea vertical punteada + tooltip ---
            selectedIndex?.let { idx ->
                val safeIdx = idx.coerceIn(0, data.lastIndex)
                val x = leftPadding + safeIdx * stepX

                drawLine(
                    color = Color.Gray,
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                val tooltipText = if (isAnnual)
                    "Mes ${data[safeIdx].day}: ${formatWeight(data[safeIdx].totalWeight)} Kg"
                else
                    "Día ${data[safeIdx].day}: ${formatWeight(data[safeIdx].totalWeight)} Kg"

                val measured = textMeasurer.measure(tooltipText, textStyle)

                drawRect(
                    color = Color.White,
                    topLeft = Offset(
                        x - measured.size.width / 2 - 4.dp.toPx(),
                        chartHeight - data[safeIdx].totalWeight * scaleY - measured.size.height - 8.dp.toPx()
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        measured.size.width + 8.dp.toPx(),
                        measured.size.height + 4.dp.toPx()
                    )
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = tooltipText,
                    topLeft = Offset(
                        x - measured.size.width / 2,
                        chartHeight - data[safeIdx].totalWeight * scaleY - measured.size.height - 6.dp.toPx()
                    ),
                    style = textStyle
                )
            }

            // --- Eje X ---
            val firstLabel = if (isAnnual) "Mes ${data.first().day}" else "Día ${data.first().day}"
            val lastLabel = if (isAnnual) "Mes ${data.last().day}" else "Día ${data.last().day}"

            drawText(textMeasurer, firstLabel, Offset(leftPadding, chartHeight + 40f), textStyle)
            drawText(
                textMeasurer,
                lastLabel,
                Offset(
                    leftPadding + chartWidth - textMeasurer.measure(lastLabel, textStyle).size.width,
                    chartHeight + 40f
                ),
                textStyle
            )
        }
    }
}
