package com.alius.gmrstock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alius.gmrstock.ui.theme.PrimaryColor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.CoreGraphics.CGPointMake
import platform.UIKit.*

/**
 * Helper nativo para dibujar texto en iOS
 */
@OptIn(ExperimentalForeignApi::class)
fun drawTextAt(x: Double, y: Double, text: String, fontSize: Double = 12.0) {
    val nsText: NSString = NSString.create(string = text)
    nsText.drawAtPoint(
        point = CGPointMake(x, y),
        withAttributes = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.systemFontOfSize(fontSize),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
    )
}

@Composable
actual fun RatioProductionCard(modifier: Modifier) {
    val data = generateRatioData()
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
            val maxWeight = 100_000f
            val stepX = chartWidth / (data.size - 1)
            val scaleY = chartHeight / maxWeight

            // Eje Y con etiquetas
            val yLabels = listOf(0f, 20000f, 40000f, 60000f, 80000f, 100000f)
            val yLabelStrings = listOf("0", "20K", "40K", "60K", "80K", "100K")
            yLabels.forEachIndexed { index, value ->
                val y = chartHeight - value * scaleY
                drawLine(
                    color = Color.LightGray,
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y)
                )
                // Etiqueta Y nativa
                drawTextAt(
                    x = 0.0,
                    y = y.toDouble(),
                    text = yLabelStrings[index],
                    fontSize = 12.0
                )
            }

            // Línea de datos
            for (i in 0 until data.size - 1) {
                val x1 = leftPadding + i * stepX
                val y1 = chartHeight - data[i].listRatioTotalWeight * scaleY
                val x2 = leftPadding + (i + 1) * stepX
                val y2 = chartHeight - data[i + 1].listRatioTotalWeight * scaleY
                drawLine(
                    color = PrimaryColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }

            // Eje X: Día 1 y último día
            val baseY = chartHeight + 20f
            drawTextAt(
                x = leftPadding.toDouble(),
                y = baseY.toDouble(),
                text = "Día 1",
                fontSize = 12.0
            )
            drawTextAt(
                x = (leftPadding + (data.size - 1) * stepX - 30f).toDouble(),
                y = baseY.toDouble(),
                text = "Día ${data.size}",
                fontSize = 12.0
            )
        }
    }
}
