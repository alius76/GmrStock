package com.alius.gmrstock.data


actual fun getVentaRepository(databaseUrl: String): VentaRepository {
    val client = createHttpClient()
    return VentaRepositoryImpl(client, databaseUrl)
}