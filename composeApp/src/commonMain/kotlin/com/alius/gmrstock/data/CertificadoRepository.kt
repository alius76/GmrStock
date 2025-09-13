package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Certificado

interface CertificadoRepository {
    suspend fun getCertificadoByLoteNumber(loteNumber: String): Certificado?
}

// ✅ expect unificado
expect fun getCertificadoRepository(databaseUrl: String): CertificadoRepository
