package com.alius.gmrstock.data

actual fun getTrasvaseRepository(databaseUrl: String): TrasvaseRepository {
    val client = createHttpClient()
    return TrasvaseRepositoryImpl(client, databaseUrl)
}


