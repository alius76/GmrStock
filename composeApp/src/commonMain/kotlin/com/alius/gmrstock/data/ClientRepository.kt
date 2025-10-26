package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Cliente

interface ClientRepository {

    suspend fun getAllClientsOrderedByName(): List<Cliente>

    suspend fun getAllClientsWithIds(): List<Pair<String, Cliente>>


    suspend fun addClient(cliente: Cliente): String


    suspend fun updateClient(documentId: String, cliente: Cliente)


    suspend fun deleteClient(documentId: String)
}

expect fun getClientRepository(databaseUrl: String): ClientRepository