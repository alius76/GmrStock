package com.alius.gmrstock.data

import com.alius.gmrstock.data.HistorialRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del HistorialRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (HistorialRepositoryImpl).
 */
actual fun getHistorialRepository(databaseUrl: String): HistorialRepository {
    val client = createHttpClient()
    return HistorialRepositoryImpl(client, databaseUrl)
}

