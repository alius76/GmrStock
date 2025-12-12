package com.alius.gmrstock.data

import com.alius.gmrstock.data.ReprocesarRepository

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del ReprocesarRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (ReprocesarRepositoryImpl).
 */
actual fun getReprocesarRepository(databaseUrl: String): ReprocesarRepository {
    val client = createHttpClient()
    return ReprocesarRepositoryImpl(client, databaseUrl)
}

