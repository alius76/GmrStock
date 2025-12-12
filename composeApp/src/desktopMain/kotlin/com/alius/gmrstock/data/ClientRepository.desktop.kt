package com.alius.gmrstock.data

import com.alius.gmrstock.data.ClientRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del ClientRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (ClientRepositoryImpl).
 */
actual fun getClientRepository(databaseUrl: String): ClientRepository {
    val client = createHttpClient()
    return ClientRepositoryImpl(client, databaseUrl)
}

