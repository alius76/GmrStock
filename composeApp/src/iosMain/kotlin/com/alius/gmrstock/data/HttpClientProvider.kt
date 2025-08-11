package com.alius.gmrstock.data

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    // Configuraciones adicionales si quieres
}