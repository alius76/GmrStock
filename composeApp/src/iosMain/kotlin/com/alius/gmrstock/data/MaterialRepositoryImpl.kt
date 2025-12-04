package com.alius.gmrstock.data

import io.ktor.client.*

actual fun getMaterialRepository(databaseUrl: String): MaterialRepository {
    val client = createHttpClient()
    return MaterialRepositoryImpl(client, databaseUrl)
}