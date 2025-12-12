package com.alius.gmrstock.data

import com.alius.gmrstock.data.TrasvaseRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del TrasvaseRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (TrasvaseRepositoryImpl).
 */
actual fun getTrasvaseRepository(databaseUrl: String): TrasvaseRepository {
    val client = createHttpClient()
    return TrasvaseRepositoryImpl(client, databaseUrl)
}

