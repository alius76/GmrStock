package com.alius.gmrstock

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform