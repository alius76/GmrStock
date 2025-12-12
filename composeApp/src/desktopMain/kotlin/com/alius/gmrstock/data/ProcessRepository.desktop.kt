package com.alius.gmrstock.data

import com.alius.gmrstock.data.ProcessRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del ProcessRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (ProcessRepositoryImpl).
 */
actual fun getProcessRepository(databaseUrl: String): ProcessRepository {
    val client = createHttpClient()
    return ProcessRepositoryImpl(client, databaseUrl)
}

