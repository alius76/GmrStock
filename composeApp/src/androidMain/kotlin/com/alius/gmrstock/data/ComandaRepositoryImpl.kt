package com.alius.gmrstock.data

actual fun getComandaRepository(databaseUrl: String): ComandaRepository {
    val client = createHttpClient()
    return ComandaRepositoryImpl(client, databaseUrl)
}