package com.alius.gmrstock.data

actual fun getRatioRepository(databaseUrl: String): RatioRepository {
    val client = createHttpClient()
    return RatioRepositoryImpl(client, databaseUrl)
}

