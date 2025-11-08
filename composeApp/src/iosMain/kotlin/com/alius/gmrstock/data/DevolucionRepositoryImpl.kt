package com.alius.gmrstock.data

import io.ktor.client.*

actual fun getDevolucionRepository(databaseUrl: String): DevolucionRepository {
    val client = createHttpClient()
    return DevolucionRepositoryImpl(client, databaseUrl)
}

