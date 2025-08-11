package com.alius.gmrstock.data

actual fun getLoteRepository(): LoteRepository {
    val client = createHttpClient()
    return LoteRepositoryImpl(client)
}
