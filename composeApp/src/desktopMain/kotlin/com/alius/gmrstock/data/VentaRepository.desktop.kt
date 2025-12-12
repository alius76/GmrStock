package com.alius.gmrstock.data

import com.alius.gmrstock.data.VentaRepository

/**
 * Implementación 'actual' FINAL para Desktop (JVM) del constructor del VentaRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (VentaRepositoryImpl).
 */
actual fun getVentaRepository(databaseUrl: String): VentaRepository {
    val client = createHttpClient()
    return VentaRepositoryImpl(client, databaseUrl)
}

