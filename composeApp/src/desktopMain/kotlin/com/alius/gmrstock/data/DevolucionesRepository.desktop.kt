package com.alius.gmrstock.data

import com.alius.gmrstock.data.DevolucionesRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del DevolucionesRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (DevolucionesRepositoryImpl).
 */
actual fun getDevolucionesRepository(databaseUrl: String): DevolucionesRepository {
    val client = createHttpClient()
    return DevolucionesRepositoryImpl(client, databaseUrl)
}

