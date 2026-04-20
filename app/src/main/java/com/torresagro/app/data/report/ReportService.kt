package com.torresagro.app.data.report

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.torresagro.app.domain.model.AgriData
import com.torresagro.app.domain.model.Parcel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportService(private val context: Context) {

    fun generateParcelReport(parcel: Parcel, agriData: AgriData?): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
        }

        // Página 1: Resumen General
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 60f
        canvas.drawText("Reporte de Parcela: ${parcel.name}", 50f, y, titlePaint)
        y += 40f

        canvas.drawText("Cultivo: ${parcel.cropType.displayName}", 50f, y, textPaint)
        y += 25f
        canvas.drawText("Ubicación: ${parcel.locationName}", 50f, y, textPaint)
        y += 25f
        canvas.drawText("Área: ${parcel.sizeHectares} Ha", 50f, y, textPaint)
        y += 25f
        canvas.drawText("Fecha de Siembra: ${parcel.sowingDate}", 50f, y, textPaint)
        y += 40f

        canvas.drawText("Indicadores Actuales", 50f, y, titlePaint.apply { textSize = 18f })
        y += 30f

        if (agriData != null) {
            canvas.drawText("Humedad del Suelo: ${"%.1f".format(agriData.soilMoisture)}%", 70f, y, textPaint)
            y += 25f
            canvas.drawText("Vigor (NDVI): ${if (agriData.ndvi > 0) "%.2f".format(agriData.ndvi) else "N/A"}", 70f, y, textPaint)
            y += 25f
            canvas.drawText("Fuente de Datos: ${agriData.satelliteSource}", 70f, y, textPaint)
            y += 40f

            if (agriData.historicalGrids.isNotEmpty()) {
                canvas.drawText("Historial de los últimos 7 días", 50f, y, titlePaint.apply { textSize = 16f })
                y += 30f
                agriData.historicalGrids.forEach { grid ->
                    canvas.drawText("${grid.date}: Lluvia: ${grid.precipitation}mm | Temp Max: ${grid.tempMax}°C", 70f, y, textPaint)
                    y += 20f
                }
            }
        } else {
            canvas.drawText("No hay datos agronómicos disponibles para este reporte.", 70f, y, textPaint)
        }

        pdfDocument.finishPage(page)

        // Guardar el archivo
        val fileName = "Reporte_${parcel.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
