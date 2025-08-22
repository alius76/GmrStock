package com.alius.gmrstock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*

class RatioProductionUIView(
    frame: CValue<CGRect>,
    val data: List<RatioData>
) : UIView(frame) {

    @OptIn(ExperimentalForeignApi::class)
    override fun draw(rect: CValue<CGRect>) {
        val context = UIGraphicsGetCurrentContext() ?: return
        val width = rect.size.width
        val height = rect.size.height

        val leftPadding = 50.0
        val bottomPadding = 30.0
        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding
        val maxWeight = 100_000.0

        val backgroundColor = UIColor.whiteColor.CGColor
        val lineColor = UIColor.systemBlueColor.CGColor
        val axisColor = UIColor.lightGrayColor.CGColor
        val textColor = UIColor.blackColor

        // Fondo
        context.setFillColor(backgroundColor)
        context.fillRect(rect)

        if (data.isEmpty()) return

        // Eje Y abreviado
        val yLabels = listOf(0.0, 20_000.0, 40_000.0, 60_000.0, 80_000.0, 100_000.0)
        val yLabelStrings = listOf("0", "20K", "40K", "60K", "80K", "100K")
        context.setLineWidth(1.0)
        context.setStrokeColor(axisColor)
        yLabels.forEachIndexed { index, value ->
            val y = chartHeight - value / maxWeight * chartHeight
            context.moveTo(leftPadding, y)
            context.addLineTo(leftPadding + chartWidth, y)
            context.strokePath()

            val label = NSString.stringWithString(yLabelStrings[index])
            label.drawAtPoint(
                CGPointMake(0.0, y - 7.0),
                mapOf(
                    NSFontAttributeName to UIFont.systemFontOfSize(12.0),
                    NSForegroundColorAttributeName to textColor
                )
            )
        }

        // Línea de datos
        context.setStrokeColor(lineColor)
        context.setLineWidth(2.0)
        for (i in 0 until data.size - 1) {
            val x1 = leftPadding + i * chartWidth / (data.size - 1)
            val y1 = chartHeight - data[i].listRatioTotalWeight / maxWeight * chartHeight
            val x2 = leftPadding + (i + 1) * chartWidth / (data.size - 1)
            val y2 = chartHeight - data[i + 1].listRatioTotalWeight / maxWeight * chartHeight
            context.moveTo(x1, y1)
            context.addLineTo(x2, y2)
            context.strokePath()
        }

        // Eje X: primer y último día
        val xIndices = listOf(0, data.size - 1)
        xIndices.forEach { i ->
            val x = leftPadding + i * chartWidth / (data.size - 1)
            val date = NSDate(timeIntervalSince1970 = data[i].listRatioDate / 1000.0)
            val calendar = NSCalendar.currentCalendar
            val day = calendar.component(NSCalendarUnitDay, date)
            val label = NSString.stringWithString("$day")
            label.drawAtPoint(
                CGPointMake(x - 5.0, chartHeight + 2.0),
                mapOf(
                    NSFontAttributeName to UIFont.systemFontOfSize(12.0),
                    NSForegroundColorAttributeName to textColor
                )
            )
        }
    }
}

@Composable
actual fun RatioProductionCard(modifier: Modifier) {
    val data = generateRatioData()
    UIKitView(
        modifier = modifier,
        factory = { RatioProductionUIView(CGRectMake(0.0, 0.0, 350.0, 250.0), data) }
    )
}
