package com.alius.gmrstock.core.utils

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.FileProvider
import com.alius.gmrstock.core.AppContextProvider
import com.alius.gmrstock.domain.model.Comanda
import java.io.File
import java.io.FileOutputStream
import kotlinx.datetime.*

actual object PdfGenerator {

    private const val TAG = "PdfGenerator"

    // Mapeo de colores del theme a formato Int (RGB) para Canvas nativo
    private val PrimaryPdfColor = Color.rgb(2, 144, 131)     // 0xFF029083
    private val ReservedPdfColor = Color.rgb(183, 28, 28)    // 0xFFB71C1C
    private val DarkGrayPdfColor = Color.rgb(85, 85, 85)     // 0xFF555555
    private val TextPrimaryPdf = Color.rgb(51, 51, 51)       // 0xFF333333
    private val GrayPdfColor = Color.rgb(204, 204, 204)      // 0xFFCCCCCC

    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Tamaño A4 estándar
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 45f
        var y = 60f

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Función para gestionar saltos de página automáticos
        fun checkNewPage(needed: Float) {
            if (y + needed > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
        }

        // --- LÓGICA DE FILTRADO Y AGRUPACIÓN ---
        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toSortedMap(compareBy { it })

        // --- CABECERA DEL DOCUMENTO ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor
        canvas.drawText(title.uppercase(), margin, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY
        canvas.drawText("Rango: $dateRange", margin, y, paint)

        y += 40f

        // --- LISTADO POR DÍAS ---
        groupedComandas.forEach { (date, list) ->
            checkNewPage(120f)

            // 1. Título de Fecha (Negro y Bold)
            val dateText = if (date == null) "SIN FECHA" else {
                "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
            }

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.color = Color.BLACK
            canvas.drawText(dateText, margin, y, paint)

            y += 10f
            paint.color = GrayPdfColor
            paint.strokeWidth = 1f
            canvas.drawLine(margin, y, pageWidth - margin, y, paint)
            y += 25f

            list.forEach { comanda ->
                checkNewPage(110f)

                // 2. Nombre del Cliente (Gris oscuro y Bold como el título)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 13f
                paint.color = DarkGrayPdfColor
                val clienteNombre = comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente"
                canvas.drawText(clienteNombre, margin + 15f, y, paint)

                // 3. Etiqueta de RETRASADA (A la derecha del nombre del cliente)
                if (date != null && date < today) {
                    val labelPaint = Paint().apply {
                        color = ReservedPdfColor
                        style = Paint.Style.FILL
                    }
                    val nameWidth = paint.measureText(clienteNombre)
                    val labelX = margin + 15f + nameWidth + 15f

                    // Fondo de la etiqueta redondeada
                    val labelRect = RectF(labelX, y - 16f, labelX + 85f, y + 4f)
                    canvas.drawRoundRect(labelRect, 6f, 6f, labelPaint)

                    // Texto centrado en la etiqueta roja
                    paint.color = Color.WHITE
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val textLabel = "RETRASADA"
                    val textWidth = paint.measureText(textLabel)
                    // Centrado horizontal y vertical corregido
                    canvas.drawText(textLabel, labelX + (85f - textWidth) / 2f, y - 3f, paint)

                    // Restaurar tamaño de pincel para los siguientes campos
                    paint.textSize = 13f
                }

                y += 18f
                // 4. Descripción del Material
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 11f
                paint.color = TextPrimaryPdf
                canvas.drawText("Material: ${comanda.descriptionLoteComanda}", margin + 25f, y, paint)

                y += 16f
                // 5. Peso Total (Usando tu función formatWeight exacta)
                val pesoStr = "Peso total: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg"
                canvas.drawText(pesoStr, margin + 25f, y, paint)

                y += 18f
                // 6. Estado y Número de Lote (Colores Primary vs Reserved)
                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                val loteText = if (isAssigned) "Lote asignado: ${comanda.numberLoteComanda}" else "PENDIENTE ASIGNAR LOTE"

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (isAssigned) PrimaryPdfColor else ReservedPdfColor
                canvas.drawText(loteText, margin + 25f, y, paint)

                // 7. Observaciones (en cursiva si existen)
                if (!comanda.remarkComanda.isNullOrBlank()) {
                    y += 16f
                    paint.color = Color.GRAY
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 10f
                    canvas.drawText("Obs: ${comanda.remarkComanda}", margin + 25f, y, paint)
                }

                y += 35f // Margen inferior entre comandas
            }
            y += 10f // Espacio extra al terminar el grupo del día
        }

        pdfDocument.finishPage(page)

        // --- PROCESO DE GUARDADO Y COMPARTIR ---
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

            // Generación de URI segura vía FileProvider para compartir fuera de la app
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

            val chooser = Intent.createChooser(shareIntent, "Compartir Planning")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar o compartir PDF: ${e.message}")
        }
    }
}