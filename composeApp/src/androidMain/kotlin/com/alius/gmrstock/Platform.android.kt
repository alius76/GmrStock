package com.alius.gmrstock

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isAuthSupported: Boolean = true
    override val isMobile: Boolean = true
}

actual fun getPlatform(): Platform = AndroidPlatform()