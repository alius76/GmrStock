package com.alius.gmrstock.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

// Mapa de nombres del commonMain a nombres de assets en Xcode
private val logoMap = mapOf(
    "logo.png" to "logo",
    "gmr_stock_p07.png" to "gmr_stock_p07",
    "gmr_stock_p08.png" to "gmr_stock_p08"
)

/**
 * Carga la imagen desde el asset catalog de iOS.
 * @param fileName nombre de la imagen en commonMain
 * @return UIImage o null si no se encuentra
 */
actual fun loadPlatformImage(fileName: String): Any? {
    val resourceName = logoMap[fileName] ?: return null
    return UIImage.imageNamed(resourceName)
}

/**
 * Composable que recibe un UIImage y lo muestra dentro de Compose.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformImageComposable(image: Any?, modifier: Modifier) {
    if (image is UIImage) {
        UIKitView(
            modifier = modifier,
            factory = {
                UIImageView().apply {
                    contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                    this.image = image
                }
            }
        )
    }
}