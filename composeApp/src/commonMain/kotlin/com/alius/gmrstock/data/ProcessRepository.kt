package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Process

interface ProcessRepository {
    // Trae todos los procesos
    suspend fun listarProcesos(): List<Process>
}

// Función expect para obtener la implementación según plataforma
expect fun getProcessRepository(databaseUrl: String): ProcessRepository