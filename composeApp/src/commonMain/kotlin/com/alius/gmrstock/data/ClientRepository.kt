package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Cliente

interface ClientRepository {
    suspend fun getAllClientsOrderedByName(): List<Cliente>
}

expect fun getClientRepository(databaseUrl: String): ClientRepository