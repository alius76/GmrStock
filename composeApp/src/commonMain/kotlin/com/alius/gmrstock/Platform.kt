package com.alius.gmrstock

interface Platform {
    val name: String
    val isAuthSupported: Boolean
}

expect fun getPlatform(): Platform