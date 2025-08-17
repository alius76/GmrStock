package com.alius.gmrstock.data

import com.alius.gmrstock.R
import com.alius.gmrstock.core.AppContextProvider
import java.io.File

private val logoMap = mapOf(
    "logo.png" to R.raw.logo,
    "gmr_stock_p07.png" to R.raw.gmr_stock_p07,
    "gmr_stock_p08.png" to R.raw.gmr_stock_p08
    // Agrega más imágenes aquí según sea necesario
)

actual fun getDatabaseLogoPath(fileName: String): String {
    val context = AppContextProvider.appContext
    println("📌 [DatabaseLogoPath] Obteniendo logo Android: $fileName")

    val resId = logoMap[fileName] ?: throw IllegalArgumentException("Logo no encontrado: $fileName")

    val inputStream = context.resources.openRawResource(resId)
    val file = File(context.cacheDir, fileName)
    inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }

    println("📌 [DatabaseLogoPath] Logo copiado a: ${file.absolutePath}")
    return file.absolutePath
}
