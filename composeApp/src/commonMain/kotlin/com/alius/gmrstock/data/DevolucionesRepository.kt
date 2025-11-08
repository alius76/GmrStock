package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.LoteModel

interface DevolucionesRepository {

    /**
     * Devuelve un lote específico por su número.
     */
    suspend fun getLoteByNumber(number: String): LoteModel?

    /**
     * Marca un BigBag individual como devuelto:
     * - Cambia bbStatus = "s", bbRemark = "DEVO".
     * - Actualiza count +1 y totalWeight + bbWeight en la base de datos.
     * * @param loteNumber El número del lote a actualizar.
     * @param bigBagNumber El número de BigBag a devolver.
     * @return True si la actualización fue exitosa.
     */
    suspend fun devolverBigBag(loteNumber: String, bigBagNumber: String): Boolean

    /**
     * Marca MÚLTIPLES BigBags como devueltos en una única operación:
     * - Cambia bbStatus = "s", bbRemark = "DEVO" para los BigBags indicados.
     * - **CALCULA** el nuevo count y totalWeight basado en el estado actual del lote
     * y los BigBags devueltos, y lo actualiza en la base de datos.
     *
     * ⚠️ Hemos eliminado newCount y newTotalWeight de la firma para simplificar
     * la llamada desde la capa de presentación (UI).
     * * @param loteNumber El número del lote a actualizar.
     * @param bigBagNumbers La lista de números de BigBags a devolver.
     * @return True si la actualización fue exitosa.
     */
    suspend fun devolverBigBags(
        loteNumber: String,
        bigBagNumbers: List<String>
    ): Boolean
}

// Función expect para la inyección multiplataforma
expect fun getDevolucionesRepository(databaseUrl: String): DevolucionesRepository