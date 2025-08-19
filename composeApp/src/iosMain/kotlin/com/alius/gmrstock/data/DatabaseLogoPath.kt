package com.alius.gmrstock.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

import platform.Foundation.NSBundle
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

private val logoMap = mapOf(
    "logo.png" to "logo",
    "gmr_stock_p07.png" to "gmr_stock_p07",
    "gmr_stock_p08.png" to "gmr_stock_p08"
)

actual fun loadPlatformImage(fileName: String): Any? {
    val resourceName = logoMap[fileName] ?: return null
    val path = NSBundle.mainBundle.pathForResource(resourceName, "png") ?: return null
    return UIImage.imageWithContentsOfFile(path)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformImageComposable(image: Any?, modifier: Modifier) {
    if (image is UIImage) {
        UIKitView(
            modifier = modifier,
            factory = {
                UIImageView().apply {
                    this.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                    this.image = image
                }
            }
        )
    }
}
