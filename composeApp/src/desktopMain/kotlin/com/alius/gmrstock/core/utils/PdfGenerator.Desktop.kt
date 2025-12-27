package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda
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

    /**
     * Limpia el texto de saltos de línea y caracteres especiales que Helvetica no puede renderizar
     */
    private fun String.pdfSafe(): String {
        return this.replace("\n", " ").replace("\r", " ").trim()
    }

    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val document = PDDocument()
        val margin = 45f
        val pageWidth = PDRectangle.A4.width
        var y = PDRectangle.A4.height - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun checkNewPage(needed: Float) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = PDRectangle.A4.height - 60f
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

        y -= 22f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Rango: ${dateRange.pdfSafe()}")
        contentStream.endText()

        y -= 40f

        // --- CONTENIDO ---
        groupedComandas.forEach { (date, list) ->
            checkNewPage(100f)

            val dateText = if (date == null) "SIN FECHA" else {
                "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
            }

            // 1. Fecha
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
            contentStream.setNonStrokingColor(Color.BLACK)
            contentStream.newLineAtOffset(margin, y)
            contentStream.showText(dateText)
            contentStream.endText()

            y -= 10f
            contentStream.setStrokingColor(GrayPdfColor)
            contentStream.setLineWidth(1f)
            contentStream.moveTo(margin, y)
            contentStream.lineTo(pageWidth - margin, y)
            contentStream.stroke()
            y -= 25f

            list.forEach { comanda ->
                checkNewPage(100f)

                // 2. Cliente
                val clienteNombre = (comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente").pdfSafe()
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13f)
                contentStream.setNonStrokingColor(DarkGrayPdfColor)
                contentStream.newLineAtOffset(margin + 15f, y)
                contentStream.showText(clienteNombre)
                contentStream.endText()

                // 3. Etiqueta RETRASADA
                if (date != null && date < today) {
                    val nameWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(clienteNombre) / 1000 * 13f
                    val labelX = margin + 15f + nameWidth + 10f
                    val labelWidth = 75f
                    val labelHeight = 15f

                    contentStream.setNonStrokingColor(ReservedPdfColor)
                    contentStream.addRect(labelX, y - 3f, labelWidth, labelHeight)
                    contentStream.fill()

                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 8f)
                    contentStream.setNonStrokingColor(Color.WHITE)
                    val textLabel = "RETRASADA"
                    val textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(textLabel) / 1000 * 8f
                    contentStream.newLineAtOffset(labelX + (labelWidth - textWidth) / 2, y + 1f)
                    contentStream.showText(textLabel)
                    contentStream.endText()
                }

                y -= 18f
                // 4. Material
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA, 11f)
                contentStream.setNonStrokingColor(TextPrimaryPdf)
                contentStream.newLineAtOffset(margin + 25f, y)
                contentStream.showText("Material: ${comanda.descriptionLoteComanda.pdfSafe()}")
                contentStream.endText()

                y -= 16f
                // 5. Peso Total
                val pesoStr = "Peso total: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg"
                contentStream.beginText()
                contentStream.newLineAtOffset(margin + 25f, y)
                contentStream.showText(pesoStr.pdfSafe())
                contentStream.endText()

                y -= 18f
                // 6. Lote
                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                val loteText = if (isAssigned) "Lote asignado: ${comanda.numberLoteComanda}" else "PENDIENTE ASIGNAR LOTE"

                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
                contentStream.setNonStrokingColor(if (isAssigned) PrimaryPdfColor else ReservedPdfColor)
                contentStream.newLineAtOffset(margin + 25f, y)
                contentStream.showText(loteText.pdfSafe())
                contentStream.endText()

                // 7. Observaciones (Solución definitiva al error de controlLF)
                if (!comanda.remarkComanda.isNullOrBlank()) {
                    y -= 16f
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10f)
                    contentStream.setNonStrokingColor(Color.GRAY)
                    contentStream.newLineAtOffset(margin + 25f, y)
                    // Se limpia el texto de saltos de línea aquí
                    contentStream.showText("Obs: ${comanda.remarkComanda.pdfSafe()}")
                    contentStream.endText()
                }

                y -= 35f
            }
            y -= 10f
        }

        contentStream.close()
        savePdfDesktop(document, "Planning_Comandas")
    }

    private fun savePdfDesktop(document: PDDocument, fileName: String) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Guardar Planning PDF"
            fileFilter = FileNameExtensionFilter("Documento PDF", "pdf")
            selectedFile = File("$fileName.pdf")
        }

        val userSelection = fileChooser.showSaveDialog(null)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val fileToSave = fileChooser.selectedFile
            val finalFile = if (fileToSave.absolutePath.endsWith(".pdf")) fileToSave else File("${fileToSave.absolutePath}.pdf")

            document.save(finalFile)
            document.close()

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(finalFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            document.close()
        }
    }
}