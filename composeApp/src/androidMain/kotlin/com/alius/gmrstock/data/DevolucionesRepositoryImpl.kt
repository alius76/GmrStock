package com.alius.gmrstock.data

actual fun getDevolucionesRepository(databaseUrl: String): DevolucionesRepository {
    val client = createHttpClient()
    return DevolucionesRepositoryImpl(client, databaseUrl)
}