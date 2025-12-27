package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda
import kotlinx.cinterop.*
import kotlinx.datetime.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*


@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual object PdfGenerator {

    private val primaryColor = UIColor.colorWithRed(2.0/255.0, green = 144.0/255.0, blue = 131.0/255.0, alpha = 1.0)
    private val reservedColor = UIColor.colorWithRed(183.0/255.0, green = 28.0/255.0, blue = 28.0/255.0, alpha = 1.0)
    private val darkGrayColor = UIColor.colorWithRed(85.0/255.0, green = 85.0/255.0, blue = 85.0/255.0, alpha = 1.0)

    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val fileName = "Planning_Comandas.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toList()
            .sortedBy { it.first }

        var currentY = 60.0

        fun checkNewPage(needed: Double) {
            if (currentY + needed > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }
        }

        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        // --- CABECERA ---
        drawText(title.uppercase(), 45.0, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)
        currentY += 30.0

        drawText("Rango: $dateRange", 45.0, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 40.0

        groupedComandas.forEach { (date: LocalDate?, list: List<Comanda>) ->
            checkNewPage(120.0)

            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

            drawText(dateText, 45.0, currentY, UIFont.boldSystemFontOfSize(16.0), UIColor.blackColor)

            currentY += 20.0
            val context = UIGraphicsGetCurrentContext()
            CGContextSetStrokeColorWithColor(context, UIColor.lightGrayColor.CGColor)
            CGContextSetLineWidth(context, 1.0)
            CGContextMoveToPoint(context, 45.0, currentY)
            CGContextAddLineToPoint(context, pageWidth - 45.0, currentY)
            CGContextStrokePath(context)
            currentY += 25.0

            list.forEach { comanda: Comanda ->
                checkNewPage(110.0)

                val clienteNombre = comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente"
                drawText(clienteNombre, 60.0, currentY, UIFont.boldSystemFontOfSize(13.0), darkGrayColor)

                if (date != null && date < today) {
                    // CORRECCIÓN: Medición de ancho compatible con CValue
                    val nsName = clienteNombre as NSString
                    val attributes = mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(13.0))
                    @Suppress("UNCHECKED_CAST")
                    val size = nsName.sizeWithAttributes(attributes as Map<Any?, *>)
                    val textWidth = size.useContents { width }
                    val labelWidth = 75.0
                    val labelX = 60.0 + textWidth + 10.0

                    val labelRect = CGRectMake(labelX, currentY, labelWidth, 16.0)
                    reservedColor.setFill()
                    UIBezierPath.bezierPathWithRoundedRect(labelRect, 4.0).fill()

                    drawTextInRect("RETRASADA", labelRect, UIFont.boldSystemFontOfSize(9.0), UIColor.whiteColor, NSTextAlignmentCenter)
                }

                currentY += 18.0
                drawText("Material: ${comanda.descriptionLoteComanda}", 70.0, currentY, UIFont.systemFontOfSize(11.0), UIColor.darkTextColor())

                currentY += 16.0
                val pesoStr = "Peso total: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg"
                drawText(pesoStr, 70.0, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)

                currentY += 18.0
                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                val loteText = if (isAssigned) "Lote asignado: ${comanda.numberLoteComanda}" else "PENDIENTE ASIGNAR LOTE"
                drawText(loteText, 70.0, currentY, UIFont.boldSystemFontOfSize(11.0), if (isAssigned) primaryColor else reservedColor)

                if (comanda.remarkComanda.isNotBlank()) {
                    currentY += 16.0
                    drawText("Obs: ${comanda.remarkComanda}", 70.0, currentY, UIFont.italicSystemFontOfSize(10.0), UIColor.grayColor)
                }
                currentY += 35.0
            }
            currentY += 10.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    // Funciones auxiliares internas para evitar conflictos de casting de tipos
    @Suppress("UNCHECKED_CAST")
    private fun drawText(text: String, x: Double, y: Double, font: UIFont, color: UIColor) {
        val attributes = mapOf(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color
        )
        (text as NSString).drawAtPoint(CGPointMake(x, y), withAttributes = attributes as Map<Any?, *>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun drawTextInRect(text: String, rect: CValue<CGRect>, font: UIFont, color: UIColor, alignment: NSTextAlignment) {
        val paragraphStyle = NSMutableParagraphStyle().apply { setAlignment(alignment) }
        val attributes = mapOf(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color,
            NSParagraphStyleAttributeName to paragraphStyle
        )
        (text as NSString).drawInRect(rect, withAttributes = attributes as Map<Any?, *>)
    }

    private fun showShareSheet(filePath: String) {
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        val window = UIApplication.sharedApplication.keyWindow
        val rootVC = window?.rootViewController

        activityViewController.popoverPresentationController?.sourceView = rootVC?.view
        rootVC?.presentViewController(activityViewController, animated = true, completion = null)
    }
}