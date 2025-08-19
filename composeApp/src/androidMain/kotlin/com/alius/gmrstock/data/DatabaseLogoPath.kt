package com.alius.gmrstock.data

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import com.alius.gmrstock.core.AppContextProvider
import java.io.File

private val logoMap = mapOf(
    "logo.png" to com.alius.gmrstock.R.raw.logo,
    "gmr_stock_p07.png" to com.alius.gmrstock.R.raw.gmr_stock_p07,
    "gmr_stock_p08.png" to com.alius.gmrstock.R.raw.gmr_stock_p08
)

actual fun loadPlatformImage(fileName: String): Any? {
    val context = AppContextProvider.appContext
    val resId = logoMap[fileName] ?: return null
    val inputStream = context.resources.openRawResource(resId)
    val file = File(context.cacheDir, fileName)
    inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
    return BitmapFactory.decodeFile(file.absolutePath)
}

@Composable
actual fun PlatformImageComposable(image: Any?, modifier: Modifier) {
    if (image is android.graphics.Bitmap) {
        Image(bitmap = image.asImageBitmap(), contentDescription = null, modifier = modifier)
    }
}
