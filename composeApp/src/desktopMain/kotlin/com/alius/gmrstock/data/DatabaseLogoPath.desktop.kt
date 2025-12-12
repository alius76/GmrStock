package com.alius.gmrstock.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.IOException
import javax.imageio.ImageIO
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * Implementación 'actual' de loadPlatformImage para Desktop (JVM).
 * Carga la imagen desde el Classpath (dentro del directorio resources).
 */
actual fun loadPlatformImage(fileName: String): Any? {
    return try {
        // Intentar cargar la imagen desde el Classpath de JVM
        val resourceStream = Thread.currentThread().contextClassLoader.getResourceAsStream(fileName)
            ?: throw IOException("Image resource not found in classpath: $fileName")

        resourceStream.use { stream ->
            ImageIO.read(stream)?.toComposeImageBitmap()
        }
    } catch (e: Exception) {
        println("Error loading image in Desktop from Classpath: $fileName. Error: ${e.message}")
        // Devolver null en caso de fallo, coincidiendo con la firma de Android (Any?)
        null
    }
}

@Composable
actual fun PlatformImageComposable(image: Any?, modifier: Modifier) {
    // El objeto cargado será un ImageBitmap de Compose
    if (image is ImageBitmap) {
        Image(bitmap = image, contentDescription = null, modifier = modifier)
    }
}