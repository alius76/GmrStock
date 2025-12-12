package com.alius.gmrstock

interface Platform {
    val name: String
    val isAuthSupported: Boolean
    val isMobile: Boolean
}

expect fun getPlatform(): Platform