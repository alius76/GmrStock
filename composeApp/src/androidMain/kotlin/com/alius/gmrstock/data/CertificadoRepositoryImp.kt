package com.alius.gmrstock.data

actual fun getCertificadoRepository(databaseUrl: String): CertificadoRepository {
    val client = createHttpClient()
    return CertificadoRepositoryImpl(client, databaseUrl)
}