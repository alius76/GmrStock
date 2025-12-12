package com.alius.gmrstock.data

import com.alius.gmrstock.data.MaterialRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del MaterialRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (MaterialRepositoryImpl).
 */
actual fun getMaterialRepository(databaseUrl: String): MaterialRepository {
    val client = createHttpClient()
    return MaterialRepositoryImpl(client, databaseUrl)
}

