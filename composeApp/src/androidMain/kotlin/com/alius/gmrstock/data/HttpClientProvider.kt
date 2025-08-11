package com.alius.gmrstock.data
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    // Configuraciones adicionales si quieres
}