package com.alius.gmrstock.data

/**
 * Implementación 'actual' para Desktop (JVM) del constructor del CertificadoRepository.
 * Reutiliza el HttpClient de Ktor para Desktop y la implementación común (CertificadoRepositoryImpl).
 */
actual fun getCertificadoRepository(databaseUrl: String): CertificadoRepository {
    val client = createHttpClient()
    return CertificadoRepositoryImpl(client, databaseUrl)
}

