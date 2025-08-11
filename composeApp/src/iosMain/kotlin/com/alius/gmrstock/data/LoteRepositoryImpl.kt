package com.alius.gmrstock.data

actual fun getLoteRepository(databaseUrl: String): LoteRepository {
    val client = createHttpClient()
    return LoteRepositoryImpl(client, databaseUrl)
}
