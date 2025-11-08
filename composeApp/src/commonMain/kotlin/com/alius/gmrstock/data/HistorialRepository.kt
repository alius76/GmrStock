package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.LoteModel

interface HistorialRepository {

    /**
     * Trae los lotes de la colección 'historial' que fueron borrados/creados
     * durante el día de hoy (createdAt = hoy).
     */
    suspend fun listarLotesHistorialDeHoy(): List<LoteModel>

    // ----------------------------------------------------
    // 🚀 Nuevas funciones para la Lógica de Devolución
    // ----------------------------------------------------

    /**
     * Busca un lote específico por su número en la colección 'historial'.
     */
    suspend fun getLoteHistorialByNumber(number: String): LoteModel?

    /**
     * Copia un LoteModel a la colección 'lote' para "resucitarlo".
     * Se usa para mover el registro de 'historial' a 'lote'.
     */
    suspend fun agregarLote(lote: LoteModel): Boolean

    // 🆕 FUNCIÓN POST + PATCH LIGADO
    /**
     * Crea un nuevo documento 'lote' usando POST (Firestore genera el ID),
     * y luego usa PATCH para actualizar el campo de datos 'id' con el ID real generado.
     * Retorna el ID real del nuevo documento o null si falla la creación o el ligado.
     */
    suspend fun agregarYLigaroLote(lote: LoteModel): String?

    /**
     * Elimina el registro del lote de la colección 'historial' después de la réplica.
     */
    suspend fun eliminarLoteHistorial(loteId: String): Boolean
}

// Función expect para la inyección de dependencias
expect fun getHistorialRepository(databaseUrl: String): HistorialRepository