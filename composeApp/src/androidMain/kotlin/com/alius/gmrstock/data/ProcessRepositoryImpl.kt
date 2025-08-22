package com.alius.gmrstock.data

actual fun getProcessRepository(databaseUrl: String): ProcessRepository {
    val client = createHttpClient()
    return ProcessRepositoryImpl(client, databaseUrl)
}