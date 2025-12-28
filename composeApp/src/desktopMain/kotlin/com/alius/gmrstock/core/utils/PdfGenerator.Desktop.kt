package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.presentation.screens.ProduccionDiaria
import com.alius.gmrstock.core.utils.formatWeight // Importación explícita
import kotlinx.datetime.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.awt.Color
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.Desktop

actual object PdfGenerator {

    private val PrimaryPdfColor = Color(2, 144, 131)
    private val ReservedPdfColor = Color(183, 28, 28)
    private val DarkGrayPdfColor = Color(85, 85, 85)
    private val TextPrimaryPdf = Color(51, 51, 51)
    private val GrayPdfColor = Color(204, 204, 204)
    private val LightGrayBg = Color(245, 245, 245)

    private fun String.pdfSafe(): String {
        return this.replace("\n", " ").replace("\r", " ").trim()
    }

    // CORRECCIÓN FECHA: Regex flexible para 1 o 2 dígitos
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

    // ============================================================
    // 1. GENERAR INFORME DE PRODUCCIÓN (CON LOGO - DESKTOP PDFBOX)
    // ============================================================
    actual fun generateProductionReportPdf(
        datosAgrupados: List<ProduccionDiaria>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String
    ) {
        val document = PDDocument()
        val margin = 45f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        fun checkNewPage(needed: Float = 30f) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        // --- CABECERA ---
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("INFORME DE PRODUCCIÓN".pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 22f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 22f
        val finalRange = ensureYearInRange(dateRange)
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Rango: ${finalRange.pdfSafe()}")
        contentStream.endText()

        y -= 45f

        // --- CUADRO RESUMEN (KPIs) ---
        contentStream.setNonStrokingColor(LightGrayBg)
        contentStream.addRect(margin, y - 60f, pageWidth - (margin * 2), 75f)
        contentStream.fill()

        contentStream.setNonStrokingColor(TextPrimaryPdf)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 25f, y - 5f)
        contentStream.showText("TOTAL PRODUCIDO")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("DÍAS ACTIVOS")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("MEDIA DIARIA")
        contentStream.endText()

        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 17f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 25f, y - 35f)
        // USO DE formatWeight EN KPIs
        contentStream.showText("${formatWeight(totalKilos)} kg")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("${datosAgrupados.size} días")
        contentStream.newLineAtOffset(185f, 0f)
        // USO DE formatWeight EN KPIs
        contentStream.showText("${formatWeight(promedio)} kg")
        contentStream.endText()

        y -= 100f

        // --- SECCIÓN: DESGLOSE MENSUAL (Visual) ---
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
            checkNewPage(datosMensuales.size * 35f)
            contentStream.setNonStrokingColor(DarkGrayPdfColor)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin, y)
            contentStream.showText("DISTRIBUCIÓN POR MES")
            contentStream.endText()
            y -= 20f

            datosMensuales.forEach { mes ->
                contentStream.setNonStrokingColor(TextPrimaryPdf)
                contentStream.setFont(PDType1Font.HELVETICA, 10f)
                contentStream.beginText()
                contentStream.newLineAtOffset(margin, y)
                contentStream.showText(mes.label)
                contentStream.endText()

                val pesoMesText = "${formatWeight(mes.kilos)} kg (${(mes.porcentaje * 100).toInt()}%)"
                val pesoWidth = PDType1Font.HELVETICA.getStringWidth(pesoMesText) / 1000 * 10f
                contentStream.beginText()
                contentStream.newLineAtOffset(pageWidth - margin - pesoWidth, y)
                contentStream.showText(pesoMesText)
                contentStream.endText()

                y -= 10f
                val barWidth = pageWidth - (margin * 2)
                contentStream.setNonStrokingColor(Color(230, 230, 230))
                contentStream.addRect(margin, y, barWidth, 6f)
                contentStream.fill()

                contentStream.setNonStrokingColor(PrimaryPdfColor)
                contentStream.addRect(margin, y, barWidth * mes.porcentaje.coerceIn(0f, 1f), 6f)
                contentStream.fill()
                y -= 25f
            }
            y -= 10f
        }

        // --- TABLA DETALLADA ---
        checkNewPage(40f)
        contentStream.setNonStrokingColor(Color.BLACK)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("DETALLE DIARIO")
        contentStream.endText()

        y -= 20f
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("FECHA")
        contentStream.newLineAtOffset(190f, 0f)
        contentStream.showText("REGISTROS")
        contentStream.endText()

        val labelPesoTab = "PESO TOTAL"
        val labelW = PDType1Font.HELVETICA_BOLD.getStringWidth(labelPesoTab) / 1000 * 10f
        contentStream.beginText()
        contentStream.newLineAtOffset(pageWidth - margin - labelW - 10f, y)
        contentStream.showText(labelPesoTab)
        contentStream.endText()

        y -= 8f
        contentStream.setStrokingColor(GrayPdfColor)
        contentStream.setLineWidth(1f)
        contentStream.moveTo(margin, y)
        contentStream.lineTo(pageWidth - margin, y)
        contentStream.stroke()
        y -= 25f

        datosAgrupados.sortedByDescending { it.fecha }.forEachIndexed { index, dia ->
            checkNewPage(30f)
            if (index % 2 != 0) {
                contentStream.setNonStrokingColor(Color(250, 250, 250))
                contentStream.addRect(margin, y - 5f, pageWidth - (margin * 2), 25f)
                contentStream.fill()
            }
            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA, 11f)

            val f = dia.fecha
            val dateStr = "${f.dayOfMonth.toString().padStart(2,'0')}/${f.monthNumber.toString().padStart(2,'0')}/${f.year}"

            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 10f, y)
            contentStream.showText(dateStr)
            contentStream.newLineAtOffset(190f, 0f)
            contentStream.showText("${dia.cantidadRegistros} lotes")
            contentStream.endText()

            val pText = "${formatWeight(dia.totalKilos)} kg"
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
            val pW = PDType1Font.HELVETICA_BOLD.getStringWidth(pText) / 1000 * 11f
            contentStream.beginText()
            contentStream.newLineAtOffset(pageWidth - margin - pW - 10f, y)
            contentStream.showText(pText)
            contentStream.endText()
            y -= 28f
        }

        contentStream.close()
        savePdfDesktop(document, "Reporte_Produccion")
    }

    // ============================================================
    // 2. GENERAR PLANNING COMANDAS (DISEÑO GRID + LOGO - DESKTOP)
    // ============================================================
    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val document = PDDocument()
        val margin = 40f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        val columnWidth = (pageWidth - (margin * 2) - 10f) / 2f
        val cellHeight = 115f
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun checkNewPage(needed: Float) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toSortedMap(compareBy { it })

        // --- CABECERA ---
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText(title.uppercase().pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 22f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 22f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Rango: ${ensureYearInRange(dateRange).pdfSafe()}")
        contentStream.endText()

        y -= 45f

        groupedComandas.forEach { (date, list) ->
            checkNewPage(155f)
            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 15f)
            contentStream.setNonStrokingColor(Color.BLACK)
            contentStream.newLineAtOffset(margin, y)
            contentStream.showText(dateText)
            contentStream.endText()

            y -= 15f

            list.forEachIndexed { index, comanda ->
                val isRightColumn = index % 2 != 0
                val xOffset = if (isRightColumn) margin + columnWidth + 10f else margin
                if (!isRightColumn && index > 0) checkNewPage(cellHeight + 10f)

                contentStream.setStrokingColor(Color.LIGHT_GRAY)
                contentStream.setLineWidth(0.5f)
                contentStream.addRect(xOffset, y - cellHeight, columnWidth, cellHeight)
                contentStream.stroke()

                var innerY = y - 20f
                val clienteNombre = (comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente").pdfSafe()
                val truncatedNombre = if (clienteNombre.length > 25) clienteNombre.take(22) + "..." else clienteNombre
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
                contentStream.setNonStrokingColor(DarkGrayPdfColor)
                contentStream.newLineAtOffset(xOffset + 10f, innerY)
                contentStream.showText(truncatedNombre)
                contentStream.endText()

                if (date != null && date < today) {
                    val labelWidth = 55f
                    val labelHeight = 14f
                    contentStream.setNonStrokingColor(ReservedPdfColor)
                    contentStream.addRect(xOffset + columnWidth - labelWidth - 8f, innerY - 2f, labelWidth, labelHeight)
                    contentStream.fill()
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 7f)
                    contentStream.setNonStrokingColor(Color.WHITE)
                    val labelTxt = "RETRASADA"
                    val tw = PDType1Font.HELVETICA_BOLD.getStringWidth(labelTxt) / 1000 * 7f
                    contentStream.newLineAtOffset(xOffset + columnWidth - 8f - (labelWidth + tw) / 2f + 5f, innerY + 1.5f)
                    contentStream.showText(labelTxt)
                    contentStream.endText()
                }

                innerY -= 18f
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA, 10f)
                contentStream.setNonStrokingColor(TextPrimaryPdf)
                contentStream.newLineAtOffset(xOffset + 10f, innerY)
                contentStream.showText("Material: ${comanda.descriptionLoteComanda.take(28).pdfSafe()}")
                contentStream.endText()

                innerY -= 15f
                contentStream.beginText()
                contentStream.newLineAtOffset(xOffset + 10f, innerY)
                // USO DE formatWeight EN COMANDAS
                contentStream.showText("Peso: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg")
                contentStream.endText()

                innerY -= 18f
                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
                contentStream.setNonStrokingColor(if (isAssigned) PrimaryPdfColor else ReservedPdfColor)
                contentStream.newLineAtOffset(xOffset + 10f, innerY)
                contentStream.showText(if (isAssigned) "Lote: ${comanda.numberLoteComanda}" else "PENDIENTE LOTE")
                contentStream.endText()

                if (!comanda.remarkComanda.isNullOrBlank()) {
                    innerY -= 14f
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8.5f)
                    contentStream.setNonStrokingColor(Color.GRAY)
                    contentStream.newLineAtOffset(xOffset + 10f, innerY)
                    val obs = if (comanda.remarkComanda!!.length > 30) comanda.remarkComanda!!.take(27) + "..." else comanda.remarkComanda!!
                    contentStream.showText("Obs: ${obs.pdfSafe()}")
                    contentStream.endText()
                }
                if (isRightColumn || index == list.size - 1) y -= (cellHeight + 10f)
            }
            y -= 20f
        }

        contentStream.close()
        savePdfDesktop(document, "Planning_Comandas")
    }

    private fun savePdfDesktop(document: PDDocument, fileName: String) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Guardar PDF"
            fileFilter = FileNameExtensionFilter("Documento PDF", "pdf")
            selectedFile = File("$fileName.pdf")
        }

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val fileToSave = fileChooser.selectedFile
            val finalFile = if (fileToSave.absolutePath.endsWith(".pdf")) fileToSave else File("${fileToSave.absolutePath}.pdf")
            document.save(finalFile)
            document.close()
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(finalFile)
        } else {
            document.close()
        }
    }
}