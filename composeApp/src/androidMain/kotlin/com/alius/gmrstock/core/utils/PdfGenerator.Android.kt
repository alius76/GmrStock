package com.alius.gmrstock.core.utils

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.FileProvider
import com.alius.gmrstock.core.AppContextProvider
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.presentation.screens.ProduccionDiaria
import com.alius.gmrstock.core.utils.formatWeight
import java.io.File
import java.io.FileOutputStream
import kotlinx.datetime.*

actual object PdfGenerator {

    private const val TAG = "PdfGenerator"

    // Colores del theme para Canvas nativo
    private val PrimaryPdfColor = Color.rgb(2, 144, 131)     // 0xFF029083
    private val ReservedPdfColor = Color.rgb(183, 28, 28)    // 0xFFB71C1C
    private val DarkGrayPdfColor = Color.rgb(85, 85, 85)     // 0xFF555555
    private val TextPrimaryPdf = Color.rgb(51, 51, 51)       // 0xFF333333
    private val GrayPdfColor = Color.rgb(204, 204, 204)      // 0xFFCCCCCC
    private val LightGrayBg = Color.rgb(245, 245, 245)       // Fondo para KPIs

    // Helper corregido para que ambas fechas del rango muestren el año correctamente
    private fun ensureYearInRange(range: String): String {
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

        // Dividimos por espacios para procesar cada palabra (ej: "1/11", "al", "28/12/2025")
        return range.split(" ").joinToString(" ") { word ->
            // Regex flexible: \d{1,2} acepta 1 o 2 dígitos.
            // Verificamos si es una fecha tipo "1/11" o "01/11" pero que NO tenga ya el año
            if (word.matches(Regex("""\d{1,2}/\d{1,2}"""))) {
                "$word/$currentYear"
            } else {
                word
            }
        }
    }

    actual fun generateProductionReportPdf(
        datosAgrupados: List<ProduccionDiaria>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 45f
        var y = 60f

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor
        canvas.drawText("INFORME DE PRODUCCIÓN", margin, y, paint)

        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        canvas.drawText(logoText, pageWidth - margin - paint.measureText(logoText), y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY

        canvas.drawText("Rango: ${ensureYearInRange(dateRange)}", margin, y, paint)

        y += 45f

        // --- CUADRO RESUMEN KPIs
        val summaryRect = RectF(margin, y, pageWidth - margin, y + 75f)
        paint.color = LightGrayBg
        canvas.drawRoundRect(summaryRect, 12f, 12f, paint)

        paint.color = TextPrimaryPdf
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL PRODUCIDO", margin + 25f, y + 25f, paint)
        canvas.drawText("DÍAS ACTIVOS", margin + 210f, y + 25f, paint)
        canvas.drawText("MEDIA DIARIA", margin + 395f, y + 25f, paint)

        paint.textSize = 17f
        paint.color = PrimaryPdfColor
        canvas.drawText("${formatWeight(totalKilos)} kg", margin + 25f, y + 55f, paint)
        canvas.drawText("${datosAgrupados.size} días", margin + 210f, y + 55f, paint)
        canvas.drawText("${formatWeight(promedio)} kg", margin + 395f, y + 55f, paint)

        y += 110f

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

        if (datosMensuales.size > 1) { // Solo mostrar si hay más de un mes para comparar
            paint.color = DarkGrayPdfColor
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DISTRIBUCIÓN POR MES", margin, y, paint)
            y += 20f

            datosMensuales.forEach { mes ->
                // Nombre del Mes y Kilos
                paint.textSize = 10f
                paint.color = TextPrimaryPdf
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(mes.label, margin, y, paint)

                val pesoMesText = "${formatWeight(mes.kilos)} kg (${(mes.porcentaje * 100).toInt()}%)"
                canvas.drawText(pesoMesText, pageWidth - margin - paint.measureText(pesoMesText), y, paint)

                y += 8f
                // Barra de progreso (Fondo)
                val barWidth = pageWidth - (margin * 2)
                val barRectFondo = RectF(margin, y, margin + barWidth, y + 6f)
                paint.color = Color.rgb(230, 230, 230)
                canvas.drawRoundRect(barRectFondo, 3f, 3f, paint)

                // Barra de progreso (Activa)
                val barRectActiva = RectF(margin, y, margin + (barWidth * mes.porcentaje), y + 6f)
                paint.color = PrimaryPdfColor
                canvas.drawRoundRect(barRectActiva, 3f, 3f, paint)

                y += 25f
            }
            y += 10f
        }

        // --- TABLA DETALLADA (Se mantiene lógica de salto de página) ---
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DETALLE DIARIO", margin, y, paint)
        y += 20f

        // Cabeceras de tabla
        paint.textSize = 10f
        canvas.drawText("FECHA", margin + 10f, y, paint)
        canvas.drawText("REGISTROS", margin + 200f, y, paint)
        val labelPeso = "PESO TOTAL"
        canvas.drawText(labelPeso, pageWidth - margin - paint.measureText(labelPeso) - 10f, y, paint)

        y += 8f
        paint.color = GrayPdfColor
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 25f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        datosAgrupados.sortedByDescending { it.fecha }.forEachIndexed { index, dia ->
            if (y > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            // Fila cebreada
            if (index % 2 != 0) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(RectF(margin, y - 16f, pageWidth - margin, y + 8f), paint)
            }

            paint.color = TextPrimaryPdf
            val f = dia.fecha
            canvas.drawText("${f.dayOfMonth.toString().padStart(2,'0')}/${f.monthNumber.toString().padStart(2,'0')}/${f.year}", margin + 10f, y, paint)
            canvas.drawText("${dia.cantidadRegistros} lotes", margin + 200f, y, paint)

            val pesoText = "${formatWeight(dia.totalKilos)} kg"
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(pesoText, pageWidth - margin - paint.measureText(pesoText) - 10f, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            y += 28f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Reporte_Produccion_${Clock.System.now().toEpochMilliseconds()}")
    }

    // ============================================================
// 2. GENERAR PLANNING COMANDAS (DISEÑO GRID + LOGO + INTELIGENTE)
// ============================================================
    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595f
        val pageHeight = 842f
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 40f
        var y = 60f

        val columnWidth = (pageWidth - (margin * 2) - 10f) / 2f
        val cellHeight = 115f

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun checkNewPage(needed: Float) {
            if (y + needed > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
        }

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toSortedMap(compareBy { it })

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor

        // Título a la izquierda
        canvas.drawText(title.uppercase(), margin, y, paint)

        // Logo "GMR Stock" a la derecha (alineado con el título)
        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        val logoWidth = paint.measureText(logoText)
        canvas.drawText(logoText, pageWidth - margin - logoWidth, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY
        canvas.drawText("Rango: ${ensureYearInRange(dateRange)}", margin, y, paint)

        y += 45f

        groupedComandas.forEach { (date, list) ->
            // --- VALIDACIÓN DE SALTO DE FECHA INTELIGENTE ---
            // Título + Espacio + Primera Fila de Celdas
            checkNewPage(155f)

            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 15f
            paint.color = Color.BLACK
            canvas.drawText(dateText, margin, y, paint)

            y += 15f

            list.forEachIndexed { index, comanda ->
                val isRightColumn = index % 2 != 0
                val xOffset = if (isRightColumn) margin + columnWidth + 10f else margin

                // Verificación de página para filas siguientes
                if (!isRightColumn && index > 0) {
                    checkNewPage(cellHeight + 10f)
                }

                // --- DIBUJO DE CELDA (GRID) ---
                paint.style = Paint.Style.STROKE
                paint.color = Color.LTGRAY
                paint.strokeWidth = 0.5f
                val rect = RectF(xOffset, y, xOffset + columnWidth, y + cellHeight)
                canvas.drawRoundRect(rect, 8f, 8f, paint)

                paint.style = Paint.Style.FILL
                var innerY = y + 20f

                // Cliente
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f
                paint.color = DarkGrayPdfColor
                val clienteNombre = comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente"
                val truncatedNombre = if (clienteNombre.length > 25) clienteNombre.take(22) + "..." else clienteNombre
                canvas.drawText(truncatedNombre, xOffset + 10f, innerY, paint)

                // Etiqueta RETRASADA
                if (date != null && date < today) {
                    val labelPaint = Paint().apply { color = ReservedPdfColor; style = Paint.Style.FILL }
                    val labelRect = RectF(xOffset + columnWidth - 65f, y + 8f, xOffset + columnWidth - 8f, y + 22f)
                    canvas.drawRoundRect(labelRect, 4f, 4f, labelPaint)
                    paint.color = Color.WHITE; paint.textSize = 7f
                    canvas.drawText("RETRASADA", labelRect.centerX() - paint.measureText("RETRASADA")/2, labelRect.centerY() + 2.5f, paint)
                }

                // Detalles (Material, Peso, Lote)
                innerY += 18f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 10f; paint.color = TextPrimaryPdf
                canvas.drawText("Material: ${comanda.descriptionLoteComanda.take(28)}", xOffset + 10f, innerY, paint)

                innerY += 15f
                canvas.drawText("Peso: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg", xOffset + 10f, innerY, paint)

                innerY += 18f
                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9f
                paint.color = if (isAssigned) PrimaryPdfColor else ReservedPdfColor
                canvas.drawText(if (isAssigned) "Lote: ${comanda.numberLoteComanda}" else "PENDIENTE LOTE", xOffset + 10f, innerY, paint)

                // Observaciones
                if (!comanda.remarkComanda.isNullOrBlank()) {
                    innerY += 14f; paint.color = Color.GRAY
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); paint.textSize = 8.5f
                    val obs = if (comanda.remarkComanda!!.length > 30) comanda.remarkComanda!!.take(27) + "..." else comanda.remarkComanda!!
                    canvas.drawText("Obs: $obs", xOffset + 10f, innerY, paint)
                }

                // Control de avance de línea Y
                if (isRightColumn || index == list.size - 1) {
                    y += cellHeight + 10f
                }
            }
            y += 20f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Planning_Comandas_${Clock.System.now().toEpochMilliseconds()}")
    }

    private fun saveAndSharePdf(pdf: PdfDocument, fileName: String) {
        val context = AppContextProvider.appContext
        val file = File(context.cacheDir, "$fileName.pdf")

        try {
            FileOutputStream(file).use { outputStream ->
                pdf.writeTo(outputStream)
            }
            pdf.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir Reporte")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar o compartir PDF: ${e.message}")
        }
    }
}