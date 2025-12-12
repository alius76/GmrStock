package com.alius.gmrstock.data

import com.alius.gmrstock.data.LoteRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del LoteRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (LoteRepositoryImpl).
 */
actual fun getLoteRepository(databaseUrl: String): LoteRepository {
    val client = createHttpClient()
    return LoteRepositoryImpl(client, databaseUrl)
}

