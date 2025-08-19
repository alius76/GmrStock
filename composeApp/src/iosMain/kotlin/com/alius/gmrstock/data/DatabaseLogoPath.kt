package com.alius.gmrstock.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import platform.Foundation.NSBundle
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSData
import platform.UIKit.UIImage

private val availableLogos = listOf("logo.png", "gmr_stock_p07.png", "gmr_stock_p08.png")

actual fun loadPlatformImage(fileName: String): Any? {
    if (!availableLogos.contains(fileName)) return null
    val path = NSBundle.mainBundle.pathForResource(fileName.removeSuffix(".png"), "png") ?: return null
    val data = NSData.dataWithContentsOfFile(path) ?: return null
    val tempFile = NSTemporaryDirectory() + fileName
    data.writeToFile(tempFile, true)
    return UIImage.imageWithContentsOfFile(tempFile)
}

@Composable
actual fun PlatformImageComposable(image: Any?, modifier: Modifier) {
    if (image is UIImage) {
        Image(bitmap = image.asImageBitmap(), contentDescription = null, modifier = modifier)
    }
}

