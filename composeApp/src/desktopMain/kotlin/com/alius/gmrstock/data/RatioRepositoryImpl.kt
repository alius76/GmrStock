package com.alius.gmrstock.data

import com.alius.gmrstock.data.RatioRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del RatioRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (RatioRepositoryImpl).
 */
actual fun getRatioRepository(databaseUrl: String): RatioRepository {
    val client = createHttpClient()
    return RatioRepositoryImpl(client, databaseUrl)
}

