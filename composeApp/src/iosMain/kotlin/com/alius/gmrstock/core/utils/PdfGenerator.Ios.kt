package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.presentation.screens.ProduccionDiaria
import com.alius.gmrstock.core.utils.formatWeight
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
    private val lightGrayBg = UIColor.colorWithRed(245.0/255.0, green = 245.0/255.0, blue = 245.0/255.0, alpha = 1.0)
    private val textPrimaryColor = UIColor.colorWithRed(51.0/255.0, green = 51.0/255.0, blue = 51.0/255.0, alpha = 1.0)

    private fun ensureYearInRange(range: String): String {
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        return range.split(" ").joinToString(" ") { word ->
            if (word.matches(Regex("""\d{1,2}/\d{1,2}"""))) {
                "$word/$currentYear"
            } else {
                word
            }
        }
    }

    // ==========================================
    // 1. GENERAR INFORME DE PRODUCCIÓN (iOS)
    // ==========================================
    actual fun generateProductionReportPdf(
        datosAgrupados: List<ProduccionDiaria>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String
    ) {
        val fileName = "Reporte_Produccion.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        val margin = 45.0

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        var currentY = 60.0

        // --- CABECERA ---
        drawText("INFORME DE PRODUCCIÓN", margin, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(22.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(22.0), primaryColor)

        currentY += 22.0
        drawText("Rango: ${ensureYearInRange(dateRange)}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 45.0

        // --- CUADRO RESUMEN (KPIs) ---
        val summaryRect = CGRectMake(margin, currentY, pageWidth - (margin * 2), 75.0)
        lightGrayBg.setFill()
        UIBezierPath.bezierPathWithRoundedRect(summaryRect, 12.0).fill()

        drawText("TOTAL PRODUCIDO", margin + 25.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("DÍAS ACTIVOS", margin + 210.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("MEDIA DIARIA", margin + 395.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)

        drawText("${formatWeight(totalKilos)} kg", margin + 25.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${datosAgrupados.size} días", margin + 210.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${formatWeight(promedio)} kg", margin + 395.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)

        currentY += 100.0

        // ==========================================
        // 📊 NUEVA SECCIÓN: DESGLOSE MENSUAL (Visual)
        // ==========================================
        val mesesEspanol = mapOf(
            1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril", 5 to "Mayo", 6 to "Junio",
            7 to "Julio", 8 to "Agosto", 9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre"
        )

        val datosMensuales = datosAgrupados.groupBy { "${it.fecha.monthNumber}-${it.fecha.year}" }
            .map { (key, lista) ->
                val partes = key.split("-")
                object {
                    val label = "${mesesEspanol[partes[0].toInt()]} ${partes[1]}"
                    val kilos = lista.sumOf { it.totalKilos }
                    val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f
                }
            }.sortedBy { it.label }

        if (datosMensuales.size > 1) {
            drawText("DISTRIBUCIÓN POR MES", margin, currentY, UIFont.boldSystemFontOfSize(12.0), darkGrayColor)
            currentY += 20.0

            datosMensuales.forEach { mes ->
                if (currentY + 40.0 > pageHeight - 60.0) {
                    UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                    currentY = 60.0
                }

                drawText(mes.label, margin, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)

                val pesoMesText = "${formatWeight(mes.kilos)} kg (${(mes.porcentaje * 100).toInt()}%)"
                val pWidth = (pesoMesText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.systemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
                drawText(pesoMesText, pageWidth - margin - pWidth, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)

                currentY += 12.0
                val barWidth = pageWidth - (margin * 2)

                // Fondo barra
                UIColor.colorWithWhite(0.9, 1.0).setFill()
                UIRectFill(CGRectMake(margin, currentY, barWidth, 6.0))

                // Progreso barra
                primaryColor.setFill()
                UIRectFill(CGRectMake(margin, currentY, barWidth * mes.porcentaje.toDouble(), 6.0))

                currentY += 25.0
            }
            currentY += 10.0
        }

        // --- TABLA DETALLADA ---
        if (currentY + 40.0 > pageHeight - 60.0) {
            UIGraphicsBeginPDFPageWithInfo(pageRect, null)
            currentY = 60.0
        }

        drawText("DETALLE DIARIO", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(12.0), UIColor.blackColor)
        currentY += 20.0
        drawText("FECHA", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), darkGrayColor)
        drawText("REGISTROS", margin + 200.0, currentY, UIFont.boldSystemFontOfSize(10.0), darkGrayColor)

        val labelPeso = "PESO TOTAL"
        val lpWidth = (labelPeso as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
        drawText(labelPeso, pageWidth - margin - lpWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), darkGrayColor)

        currentY += 15.0
        val context = UIGraphicsGetCurrentContext()
        CGContextSetStrokeColorWithColor(context, UIColor.lightGrayColor.CGColor)
        CGContextSetLineWidth(context, 1.0)
        CGContextMoveToPoint(context, margin, currentY)
        CGContextAddLineToPoint(context, pageWidth - margin, currentY)
        CGContextStrokePath(context)
        currentY += 20.0

        datosAgrupados.sortedByDescending { it.fecha }.forEachIndexed { index, dia ->
            if (currentY + 30.0 > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }

            if (index % 2 != 0) {
                val rowRect = CGRectMake(margin, currentY - 5.0, pageWidth - (margin * 2), 25.0)
                UIColor.colorWithWhite(0.98, 1.0).setFill()
                UIRectFill(rowRect)
            }

            val f = dia.fecha
            val dateStr = "${f.dayOfMonth.toString().padStart(2,'0')}/${f.monthNumber.toString().padStart(2,'0')}/${f.year}"
            drawText(dateStr, margin + 10.0, currentY, UIFont.systemFontOfSize(11.0), textPrimaryColor)
            drawText("${dia.cantidadRegistros} lotes", margin + 200.0, currentY, UIFont.systemFontOfSize(11.0), textPrimaryColor)

            val pesoText = "${formatWeight(dia.totalKilos)} kg"
            val pesoWidth = (pesoText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(11.0)) as Map<Any?, *>).useContents { width }
            drawText(pesoText, pageWidth - margin - pesoWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(11.0), textPrimaryColor)

            currentY += 28.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    // ============================================================
    // 2. GENERAR PLANNING COMANDAS (iOS)
    // ============================================================
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
        val margin = 40.0
        val columnWidth = (pageWidth - (margin * 2.0) - 10.0) / 2.0
        val cellHeight = 115.0

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var currentY = 60.0

        fun checkNewPage(needed: Double) {
            if (currentY + needed > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }
        }

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toList()
            .sortedBy { it.first }

        // --- CABECERA ---
        drawText(title.uppercase(), margin, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(22.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(22.0), primaryColor)

        currentY += 22.0
        drawText("Rango: ${ensureYearInRange(dateRange)}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 45.0

        groupedComandas.forEach { (date, list) ->
            checkNewPage(155.0)

            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
            drawText(dateText, margin, currentY, UIFont.boldSystemFontOfSize(15.0), UIColor.blackColor)
            currentY += 15.0

            list.forEachIndexed { index, comanda ->
                val isRightColumn = index % 2 != 0
                val xOffset = if (isRightColumn) margin + columnWidth + 10.0 else margin
                if (!isRightColumn && index > 0) checkNewPage(cellHeight + 10.0)

                val rect = CGRectMake(xOffset, currentY, columnWidth, cellHeight)
                UIColor.lightGrayColor.setStroke()
                UIBezierPath.bezierPathWithRoundedRect(rect, 8.0).apply { setLineWidth(0.5); stroke() }

                var innerY = currentY + 10.0
                val clienteNombre = comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente"
                val truncatedNombre = if (clienteNombre.length > 25) clienteNombre.take(22) + "..." else clienteNombre
                drawText(truncatedNombre, xOffset + 10.0, innerY, UIFont.boldSystemFontOfSize(11.0), darkGrayColor)

                if (date != null && date < today) {
                    val labelRect = CGRectMake(xOffset + columnWidth - 65.0, currentY + 8.0, 57.0, 14.0)
                    reservedColor.setFill()
                    UIBezierPath.bezierPathWithRoundedRect(labelRect, 4.0).fill()
                    drawTextInRect("RETRASADA", labelRect, UIFont.boldSystemFontOfSize(7.0), UIColor.whiteColor, NSTextAlignmentCenter)
                }

                innerY += 18.0
                drawText("Material: ${comanda.descriptionLoteComanda.take(28)}", xOffset + 10.0, innerY, UIFont.systemFontOfSize(10.0), textPrimaryColor)
                innerY += 15.0
                drawText("Peso: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg", xOffset + 10.0, innerY, UIFont.systemFontOfSize(10.0), UIColor.grayColor)
                innerY += 18.0

                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                drawText(if (isAssigned) "Lote: ${comanda.numberLoteComanda}" else "PENDIENTE LOTE", xOffset + 10.0, innerY, UIFont.boldSystemFontOfSize(9.0), if (isAssigned) primaryColor else reservedColor)

                if (comanda.remarkComanda.isNotBlank()) {
                    innerY += 14.0
                    val obs = if (comanda.remarkComanda.length > 30) comanda.remarkComanda.take(27) + "..." else comanda.remarkComanda
                    drawText("Obs: $obs", xOffset + 10.0, innerY, UIFont.italicSystemFontOfSize(8.5), UIColor.grayColor)
                }

                if (isRightColumn || index == list.size - 1) currentY += cellHeight + 10.0
            }
            currentY += 20.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    @Suppress("UNCHECKED_CAST")
    private fun drawText(text: String, x: Double, y: Double, font: UIFont, color: UIColor) {
        val attributes = mapOf(NSFontAttributeName to font, NSForegroundColorAttributeName to color)
        (text as NSString).drawAtPoint(CGPointMake(x, y), withAttributes = attributes as Map<Any?, *>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun drawTextInRect(text: String, rect: CValue<CGRect>, font: UIFont, color: UIColor, alignment: NSTextAlignment) {
        val para = NSMutableParagraphStyle().apply { setAlignment(alignment) }
        val attributes = mapOf(NSFontAttributeName to font, NSForegroundColorAttributeName to color, NSParagraphStyleAttributeName to para)
        (text as NSString).drawInRect(rect, withAttributes = attributes as Map<Any?, *>)
    }

    private fun showShareSheet(filePath: String) {
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(listOf(fileUrl), null)
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}