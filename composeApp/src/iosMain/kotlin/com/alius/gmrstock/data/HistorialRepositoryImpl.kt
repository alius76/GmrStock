package com.alius.gmrstock.data

actual fun getHistorialRepository(databaseUrl: String): HistorialRepository {
    val client = createHttpClient()
    return HistorialRepositoryImpl(client, databaseUrl)
}