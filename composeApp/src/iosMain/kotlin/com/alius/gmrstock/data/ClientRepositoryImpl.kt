package com.alius.gmrstock.data

import com.alius.gmrstock.data.ClientRepository
import io.ktor.client.*

actual fun getClientRepository(databaseUrl: String): ClientRepository {
    val client = createHttpClient() // Función común para crear HttpClient
    return ClientRepositoryImpl(client, databaseUrl)
}


