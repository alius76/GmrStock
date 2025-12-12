package com.alius.gmrstock.data

import com.alius.gmrstock.data.ComandaRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del ComandaRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (ComandaRepositoryImpl).
 */
actual fun getComandaRepository(databaseUrl: String): ComandaRepository {
    val client = createHttpClient()
    return ComandaRepositoryImpl(client, databaseUrl)
}

