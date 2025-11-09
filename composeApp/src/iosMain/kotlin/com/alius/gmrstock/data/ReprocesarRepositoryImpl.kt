package com.alius.gmrstock.data

actual fun getReprocesarRepository(databaseUrl: String): ReprocesarRepository {
    val client = createHttpClient()
    return ReprocesarRepositoryImpl(client, databaseUrl)
}