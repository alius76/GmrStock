package com.alius.gmrstock.data

import com.alius.gmrstock.data.DevolucionRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del DevolucionRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (DevolucionRepositoryImpl).
 */
actual fun getDevolucionRepository(databaseUrl: String): DevolucionRepository {
    val client = createHttpClient()
    return DevolucionRepositoryImpl(client, databaseUrl)
}

