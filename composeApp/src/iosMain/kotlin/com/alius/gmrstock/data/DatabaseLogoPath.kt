package com.alius.gmrstock.data

import platform.Foundation.NSBundle
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile

private val availableLogos = listOf(
    "logo.png",
    "gmr_stock_p07.png",
    "gmr_stock_p08.png"
    // Agrega más imágenes aquí según sea necesario
)

actual fun getDatabaseLogoPath(fileName: String): String {
    if (!availableLogos.contains(fileName)) {
        throw IllegalArgumentException("Logo no encontrado en bundle: $fileName")
    }

    println("📌 [DatabaseLogoPath] Obteniendo logo iOS: $fileName")

    val path = NSBundle.mainBundle.pathForResource(fileName.removeSuffix(".png"), "png")
        ?: throw IllegalArgumentException("Logo no encontrado en bundle: $fileName")

    val data = NSData.dataWithContentsOfFile(path)
        ?: throw IllegalStateException("No se pudo leer el logo: $fileName")

    val tempFile = NSTemporaryDirectory() + fileName
    data.writeToFile(tempFile, true)

    println("📌 [DatabaseLogoPath] Logo iOS copiado a: $tempFile")
    return tempFile
}
