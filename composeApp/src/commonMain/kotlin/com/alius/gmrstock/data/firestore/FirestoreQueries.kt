package com.alius.gmrstock.data.firestore

import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.domain.model.Devolucion
import com.alius.gmrstock.domain.model.LoteModel
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.plus
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject


fun buildQueryVentasDeHoy(inicio: Instant, fin: Instant): String {
    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "venta" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ventaFecha" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicio" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ventaFecha" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$fin" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "ventaFecha" }, "direction": "DESCENDING" }
            ]
        }
    }
    """.trimIndent()
}

fun buildQueryUltimasVentas(limit: Int = 5): String {
    return """
    {
      "structuredQuery": {
        "from": [{ "collectionId": "venta" }],
        "orderBy": [
          { "field": { "fieldPath": "ventaFecha" }, "direction": "DESCENDING" }
        ],
        "limit": $limit
      }
    }
    """.trimIndent()
}

fun buildQueryVentasPorClienteYFecha(cliente: String, inicio: Instant, fin: Instant): String {
    // Creamos una lista mutable de filtros
    val filtros = mutableListOf<String>()

    // Agregamos filtro de cliente solo si no está vacío
    if (cliente.isNotEmpty()) {
        filtros.add(
            """
            {
                "fieldFilter": {
                    "field": { "fieldPath": "ventaCliente" },
                    "op": "EQUAL",
                    "value": { "stringValue": "$cliente" }
                }
            }
            """.trimIndent()
        )
    }

    // Filtros de fecha siempre presentes
    filtros.add(
        """
        {
            "fieldFilter": {
                "field": { "fieldPath": "ventaFecha" },
                "op": "GREATER_THAN_OR_EQUAL",
                "value": { "timestampValue": "$inicio" }
            }
        }
        """.trimIndent()
    )
    filtros.add(
        """
        {
            "fieldFilter": {
                "field": { "fieldPath": "ventaFecha" },
                "op": "LESS_THAN_OR_EQUAL",
                "value": { "timestampValue": "$fin" }
            }
        }
        """.trimIndent()
    )

    // Construimos la query final
    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "venta" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [${filtros.joinToString(",")}]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "ventaFecha" }, "direction": "DESCENDING" }
            ]
        }
    }
    """.trimIndent()
}

fun buildQueryPorNumero(data: String): String {
    return if (data.isNotBlank()) {
        """
        {
            "structuredQuery": {
                "from": [{ "collectionId": "lote" }],
                "where": {
                    "compositeFilter": {
                        "op": "AND",
                        "filters": [
                            {
                                "fieldFilter": {
                                    "field": { "fieldPath": "number" },
                                    "op": "GREATER_THAN_OR_EQUAL",
                                    "value": { "stringValue": "$data" }
                                }
                            },
                            {
                                "fieldFilter": {
                                    "field": { "fieldPath": "number" },
                                    "op": "LESS_THAN",
                                    "value": { "stringValue": "${data}\uf8ff" }
                                }
                            }
                        ]
                    }
                },
                "orderBy": [
                    { "field": { "fieldPath": "number" }, "direction": "ASCENDING" }
                ]
            }
        }
        """.trimIndent()
    } else {
        """
        {
            "structuredQuery": {
                "from": [{ "collectionId": "lote" }],
                "orderBy": [
                    { "field": { "fieldPath": "number" }, "direction": "ASCENDING" }
                ]
            }
        }
        """.trimIndent()
    }
}

fun buildQueryPorNumeroExacto(number: String): String {
    return """
    {
        "structuredQuery": {
            "from": [{"collectionId": "lote"}],
            "where": {
                "fieldFilter": {
                    "field": {"fieldPath": "number"},
                    "op": "EQUAL",
                    "value": {"stringValue": "$number"}
                }
            },
            "limit": 1
        }
    }
    """.trimIndent()
}

fun buildQueryPorFecha(inicio: Instant, fin: Instant): String {
    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "lote" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "createdAt" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicio" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "createdAt" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$fin" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "createdAt" }, "direction": "DESCENDING" }
            ]
        }
    }
    """.trimIndent()

}

/**
 * Construye la query JSON para listar lotes cuya descripción comience con el valor 'data'.
 * Utiliza el campo 'description' y la técnica de rango para simular un LIKE/StartsWith.
 */
fun buildQueryPorDescripcion(data: String): String {
    return if (data.isNotBlank()) {
        """
        {
            "structuredQuery": {
                "from": [{ "collectionId": "lote" }],
                "where": {
                    "compositeFilter": {
                        "op": "AND",
                        "filters": [
                            {
                                "fieldFilter": {
                                    "field": { "fieldPath": "description" },
                                    "op": "GREATER_THAN_OR_EQUAL",
                                    "value": { "stringValue": "$data" }
                                }
                            },
                            {
                                "fieldFilter": {
                                    "field": { "fieldPath": "description" },
                                    "op": "LESS_THAN",
                                    "value": { "stringValue": "${data}\uf8ff" }
                                }
                            }
                        ]
                    }
                },
                "orderBy": [
                    { "field": { "fieldPath": "description" }, "direction": "ASCENDING" }
                ]
            }
        }
        """.trimIndent()
    } else {
        // Si no hay filtro, devuelve todos los lotes
        """
        {
            "structuredQuery": {
                "from": [{ "collectionId": "lote" }],
                "orderBy": [
                    { "field": { "fieldPath": "number" }, "direction": "ASCENDING" }
                ]
            }
        }
        """.trimIndent()
    }
}

fun buildQueryUltimosLotes(limit: Int = 5): String {
    return """
    {
      "structuredQuery": {
        "from": [{"collectionId": "lote"}],
        "orderBy": [
          {
            "field": {"fieldPath": "createdAt"},
            "direction": "DESCENDING"
          }
        ],
        "limit": $limit
      }
    }
    """.trimIndent()
}

fun buildQueryProcesoPorNumero(number: String): String {
    return """
    {
        "structuredQuery": {
            "from": [{"collectionId": "wip"}],
            "where": {
                "fieldFilter": {
                    "field": {"fieldPath": "number"},
                    "op": "EQUAL",
                    "value": {"stringValue": "$number"}
                }
            },
            "limit": 1
        }
    }
    """.trimIndent()
}

fun buildQueryProcesosPorFecha(inicio: Instant, fin: Instant): String {
    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "wip" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "date" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicio" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "date" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$fin" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "date" }, "direction": "DESCENDING" }
            ]
        }
    }
    """.trimIndent()
}

fun buildQueryUltimosProcesos(limit: Int = 10): String {
    return """
    {
      "structuredQuery": {
        "from": [{"collectionId": "wip"}],
        "orderBy": [
          {
            "field": {"fieldPath": "date"},
            "direction": "DESCENDING"
          }
        ],
        "limit": $limit
      }
    }
    """.trimIndent()
}

fun buildQueryRatiosDelMesActual(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val year = now.year
    val month = now.monthNumber

    // Primer día del mes a medianoche
    val inicioDelMes = LocalDateTime(year, month, 1, 0, 0)
        .toInstant(TimeZone.currentSystemDefault())

    // Primer día del mes siguiente a medianoche
    val primerDiaSiguienteMes = if (month == 12) {
        LocalDateTime(year + 1, 1, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())
    } else {
        LocalDateTime(year, month + 1, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())
    }

    val finDelMes = primerDiaSiguienteMes

    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "ratio" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ratioDate" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicioDelMes" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ratioDate" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$finDelMes" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "ratioDate" }, "direction": "ASCENDING" }
            ]
        }
    }
    """.trimIndent()
}


fun buildQueryRatiosDelDia(): String {
    val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    // Inicio de hoy (medianoche)
    val inicioDelDia = LocalDateTime(hoy.year, hoy.month, hoy.dayOfMonth, 0, 0, 0)
        .toInstant(TimeZone.currentSystemDefault())

    // Inicio de mañana (medianoche del día siguiente)
    val finDelDia = inicioDelDia.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "ratio" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ratioDate" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicioDelDia" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ratioDate" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$finDelDia" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "ratioDate" }, "direction": "ASCENDING" }
            ]
        }
    }
    """.trimIndent()
}

fun buildQueryCertificadoPorNumero(number: String): String {
    return """
    {
        "structuredQuery": {
            "from": [{"collectionId": "certificados"}],
            "where": {
                "fieldFilter": {
                    "field": {"fieldPath": "loteNumber"},
                    "op": "EQUAL",
                    "value": {"stringValue": "$number"}
                }
            },
            "limit": 1
        }
    }
    """.trimIndent()
}

/**
 * Construye el cuerpo JSON necesario para actualizar un único campo ('remark')
 * en Firestore usando el método PATCH.
 */
fun buildPatchBodyForRemark(newRemark: String): String {
    return buildJsonObject {
        putJsonObject("fields") {
            putJsonObject("remark") {
                // Firestore requiere que el valor de un String se envuelva en "stringValue"
                put("stringValue", newRemark)
            }
        }
    }.toString()
}

/**
 * Construye el cuerpo JSON necesario para actualizar el campo `booked` y campos relacionados
 * en Firestore usando el método PATCH.
 */
fun buildPatchBodyForBooked(
    cliente: Cliente?,
    dateBooked: Instant?,
    bookedByUser: String? = "",
    bookedRemark: String? = ""
): String {
    return buildJsonObject {
        putJsonObject("fields") {
            // booked (Cliente)
            if (cliente != null) {
                putJsonObject("booked") {
                    putJsonObject("mapValue") {
                        putJsonObject("fields") {
                            putJsonObject("cliNombre") { put("stringValue", cliente.cliNombre) }
                            putJsonObject("cliObservaciones") { put("stringValue", cliente.cliObservaciones) }
                        }
                    }
                }
            } else {
                putJsonObject("booked") { put("nullValue", null) }
            }

            // dateBooked
            putJsonObject("dateBooked") {
                if (dateBooked != null) {
                    put("timestampValue", dateBooked.toString())
                } else {
                    put("nullValue", null)
                }
            }

            // bookedByUser y bookedRemark siempre presentes
            putJsonObject("bookedByUser") { put("stringValue", bookedByUser ?: "") }
            putJsonObject("bookedRemark") { put("stringValue", bookedRemark ?: "") }
        }
    }.toString()
}


fun buildQueryHistorialDeHoy(): String {
    val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    // 1. Inicio de hoy (medianoche)
    val inicioDelDia = LocalDateTime(hoy.year, hoy.month, hoy.dayOfMonth, 0, 0, 0)
        .toInstant(TimeZone.currentSystemDefault())

    // 2. Inicio de mañana (medianoche del día siguiente)
    val finDelDia = inicioDelDia.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "historial" }], 
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "createdAt" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicioDelDia" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "createdAt" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$finDelDia" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "createdAt" }, "direction": "DESCENDING" }
            ]
        }
    }
    """.trimIndent()
}

fun buildQueryRatiosDelAnoActual(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val year = now.year

    // Primer día del año a medianoche
    val inicioDelAno = LocalDateTime(year, 1, 1, 0, 0)
        .toInstant(TimeZone.currentSystemDefault())

    // Primer día del siguiente año a medianoche
    val inicioAnoSiguiente = LocalDateTime(year + 1, 1, 1, 0, 0)
        .toInstant(TimeZone.currentSystemDefault())

    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "ratio" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ratioDate" },
                                "op": "GREATER_THAN_OR_EQUAL",
                                "value": { "timestampValue": "$inicioDelAno" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "ratioDate" },
                                "op": "LESS_THAN",
                                "value": { "timestampValue": "$inicioAnoSiguiente" }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "ratioDate" }, "direction": "ASCENDING" }
            ]
        }
    }
    """.trimIndent()
}

// 🛠️ FUNCIÓN DE ACTUALIZACIÓN DEL LOTE (NUEVA FUNCIÓN)
/**
 * Construye el cuerpo JSON necesario para actualizar la lista completa de BigBags,
 * el conteo y el peso total en un documento 'lote' usando el método PATCH.
 */
fun buildPatchBodyForBigBagStatus(
    bigBags: List<BigBags>,
    count: Int,
    totalWeight: Double
): String {
    // Función auxiliar para formatear el peso a String
    fun doubleToStringSafe(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    val countString = count.toString()
    val totalWeightString = doubleToStringSafe(totalWeight)

    return buildJsonObject {
        putJsonObject("fields") {
            // bigBag (La lista completa)
            putJsonObject("bigBag") {
                putJsonObject("arrayValue") {
                    putJsonArray("values") {
                        bigBags.forEach { bb ->
                            val bbWeightString = doubleToStringSafe(bb.bbWeight.toDoubleOrNull() ?: 0.0)
                            add(buildJsonObject {
                                putJsonObject("mapValue") {
                                    putJsonObject("fields") {
                                        put("bbNumber", buildJsonObject { put("stringValue", bb.bbNumber) })
                                        put("bbWeight", buildJsonObject { put("stringValue", bbWeightString) })
                                        put("bbLocation", buildJsonObject { put("stringValue", bb.bbLocation) })
                                        put("bbStatus", buildJsonObject { put("stringValue", bb.bbStatus) })
                                        put("bbRemark", buildJsonObject { put("stringValue", bb.bbRemark ?: "") })
                                    }
                                }
                            })
                        }
                    }
                }
            }

            // count y totalWeight (actualizados)
            putJsonObject("count") { put("stringValue", countString) }
            putJsonObject("totalWeight") { put("stringValue", totalWeightString) }
        }
    }.toString()
}

fun buildCreateBodyForClient(cliente: Cliente): String {
    // La operación POST requiere que envolvamos todos los campos en 'fields'
    return buildJsonObject {
        putJsonObject("fields") {
            // cliNombre
            putJsonObject("cliNombre") {
                put("stringValue", cliente.cliNombre)
            }
            // cliObservaciones
            putJsonObject("cliObservaciones") {
                put("stringValue", cliente.cliObservaciones)
            }
        }
    }.toString()
}

/**
 * Construye el cuerpo JSON para la operación PATCH (Actualizar) de un documento 'cliente'.
 * La estructura es la misma que la de creación, ya que PATCH sobreescribe los campos.
 */
fun buildPatchBodyForClient(cliente: Cliente): String {
    // La operación PATCH requiere que envolvamos todos los campos en 'fields'
    return buildJsonObject {
        putJsonObject("fields") {
            // cliNombre
            putJsonObject("cliNombre") {
                put("stringValue", cliente.cliNombre)
            }
            // cliObservaciones
            putJsonObject("cliObservaciones") {
                put("stringValue", cliente.cliObservaciones)
            }
        }
    }.toString()
}

/**
 * ✅ FUNCIÓN CORREGIDA
 * El campo devolucionBigbags ahora usa "arrayValue" para formar una lista
 * de objetos (mapValue), lo cual es la estructura correcta para Firestore.
 */
fun buildCreateBodyForDevolucion(devolucion: Devolucion): String {
    return buildJsonObject {
        putJsonObject("fields") {
            // Cliente, lote y material
            putJsonObject("devolucionCliente") { put("stringValue", devolucion.devolucionCliente) }
            putJsonObject("devolucionLote") { put("stringValue", devolucion.devolucionLote) }
            putJsonObject("devolucionMaterial") { put("stringValue", devolucion.devolucionMaterial) }

            // Fecha
            putJsonObject("devolucionFecha") {
                if (devolucion.devolucionFecha != null) {
                    put("timestampValue", devolucion.devolucionFecha.toString())
                } else {
                    put("nullValue", null)
                }
            }

            // Peso total
            putJsonObject("devolucionPesoTotal") {
                if (!devolucion.devolucionPesoTotal.isNullOrEmpty()) {
                    put("stringValue", devolucion.devolucionPesoTotal)
                } else {
                    put("nullValue", null)
                }
            }

            // 🌟 CORRECCIÓN CLAVE: Usar arrayValue para la lista de BigBags
            putJsonObject("devolucionBigbags") {
                putJsonObject("arrayValue") { // <-- ¡CORRECTO! Esto indica un arreglo de Firestore
                    putJsonArray("values") {
                        devolucion.devolucionBigbags
                            .filter { !it.devolucionBbNumber.isNullOrEmpty() && !it.devolucionBbWeight.isNullOrEmpty() }
                            .forEach { bb ->
                                // Cada elemento del arreglo es un mapValue
                                add(buildJsonObject {
                                    putJsonObject("mapValue") {
                                        putJsonObject("fields") {
                                            putJsonObject("devolucionBbNumber") { put("stringValue", bb.devolucionBbNumber) }
                                            putJsonObject("devolucionBbWeight") { put("stringValue", bb.devolucionBbWeight) }
                                        }
                                    }
                                })
                            }
                    }
                }
            }
        }
    }.toString()
}


fun buildPatchBodyForDevolucion(devolucion: Devolucion): String {
    return buildJsonObject {
        putJsonObject("fields") {
            // Cliente, lote y material
            putJsonObject("devolucionCliente") { put("stringValue", devolucion.devolucionCliente) }
            putJsonObject("devolucionLote") { put("stringValue", devolucion.devolucionLote) }
            putJsonObject("devolucionMaterial") { put("stringValue", devolucion.devolucionMaterial) }

            // Fecha
            putJsonObject("devolucionFecha") {
                if (devolucion.devolucionFecha != null) {
                    put("timestampValue", devolucion.devolucionFecha.toString())
                } else {
                    put("nullValue", null)
                }
            }

            // Peso total
            putJsonObject("devolucionPesoTotal") {
                if (!devolucion.devolucionPesoTotal.isNullOrEmpty()) {
                    put("stringValue", devolucion.devolucionPesoTotal)
                } else {
                    put("nullValue", null)
                }
            }

            // 🌟 CORRECCIÓN CLAVE: Envuelve la lista de maps en arrayValue
            putJsonObject("devolucionBigbags") {
                putJsonObject("arrayValue") {
                    putJsonArray("values") {
                        devolucion.devolucionBigbags
                            .filter { !it.devolucionBbNumber.isNullOrEmpty() && !it.devolucionBbWeight.isNullOrEmpty() }
                            .forEach { bb ->
                                add(buildJsonObject {
                                    putJsonObject("mapValue") {
                                        putJsonObject("fields") {
                                            putJsonObject("devolucionBbNumber") { put("stringValue", bb.devolucionBbNumber) }
                                            putJsonObject("devolucionBbWeight") { put("stringValue", bb.devolucionBbWeight) }
                                        }
                                    }
                                })
                            }
                    }
                }
            }
        }
    }.toString()
}

fun buildQueryPorNumeroExacto(number: String, collection: String = "lote"): String { // ⬅️ Añadimos 'collection' con valor por defecto
    return """
    {
        "structuredQuery": {
            "from": [{"collectionId": "$collection"}],
            "where": {
                "fieldFilter": {
                    "field": {"fieldPath": "number"},
                    "op": "EQUAL",
                    "value": {"stringValue": "$number"}
                }
            },
            "limit": 1
        }
    }
    """.trimIndent()
}

/**
 * Construye el cuerpo JSON necesario para CREAR un nuevo documento 'lote'
 * (o copiar uno de historial) usando el método POST.
 * Requiere que todos los campos del LoteModel se serialicen correctamente.
 */
fun buildCreateBodyForLote(lote: LoteModel): String {
    // Función auxiliar para formatear el peso a String (ya la tienes)
    fun doubleToStringSafe(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    return buildJsonObject {
        putJsonObject("fields") {

            // 🛑 CORRECCIÓN CLAVE: INCLUIR EL CAMPO DE DATOS "id"
            // Esto asegura que el registro en Firestore tenga la estructura que el parser de Android espera.
            putJsonObject("id") { put("stringValue", lote.id) }

            // Campos primitivos (Strings)
            putJsonObject("number") { put("stringValue", lote.number) }
            putJsonObject("description") { put("stringValue", lote.description) }
            putJsonObject("location") { put("stringValue", lote.location) }
            putJsonObject("count") { put("stringValue", lote.count) }
            putJsonObject("weight") { put("stringValue", lote.weight) }
            putJsonObject("status") { put("stringValue", lote.status) }
            putJsonObject("totalWeight") { put("stringValue", lote.totalWeight) }
            putJsonObject("remark") { put("stringValue", lote.remark) }
            putJsonObject("bookedByUser") { put("stringValue", lote.bookedByUser ?: "") }
            putJsonObject("bookedRemark") { put("stringValue", lote.bookedRemark ?: "") }
            putJsonObject("qrCode") { put("stringValue", lote.qrCode ?: "") }
            putJsonObject("certificateOk") { put("booleanValue", lote.certificateOk) }

            // Campos de Fecha (Timestamp)
            putJsonObject("date") { put("timestampValue", lote.date?.toString() ?: Clock.System.now().toString()) }
            putJsonObject("createdAt") { put("timestampValue", lote.createdAt?.toString() ?: Clock.System.now().toString()) }
            putJsonObject("dateBooked") {
                if (lote.dateBooked != null) put("timestampValue", lote.dateBooked.toString()) else put("nullValue", JsonNull)
            }

            // booked (MapValue/Cliente)
            if (lote.booked != null) {
                putJsonObject("booked") {
                    putJsonObject("mapValue") {
                        putJsonObject("fields") {
                            putJsonObject("cliNombre") { put("stringValue", lote.booked.cliNombre) }
                            putJsonObject("cliObservaciones") { put("stringValue", lote.booked.cliObservaciones) }
                        }
                    }
                }
            } else {
                putJsonObject("booked") { put("nullValue", JsonNull) } // Usar JsonNull para el valor nulo
            }

            // bigBag (ArrayValue/List<MapValue>)
            putJsonObject("bigBag") {
                putJsonObject("arrayValue") {
                    putJsonArray("values") {
                        lote.bigBag.forEach { bb ->
                            val bbWeightString = doubleToStringSafe(bb.bbWeight.toDoubleOrNull() ?: 0.0)
                            add(buildJsonObject {
                                putJsonObject("mapValue") {
                                    putJsonObject("fields") {
                                        put("bbNumber", buildJsonObject { put("stringValue", bb.bbNumber) })
                                        put("bbWeight", buildJsonObject { put("stringValue", bbWeightString) })
                                        put("bbLocation", buildJsonObject { put("stringValue", bb.bbLocation) })
                                        put("bbStatus", buildJsonObject { put("stringValue", bb.bbStatus) })
                                        put("bbRemark", buildJsonObject { put("stringValue", bb.bbRemark ?: "") })
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }.toString()
}

/**
 * Construye la query JSON para listar lotes reservados.
 * Utiliza GREATER_THAN para filtrar lotes donde 'booked.cliNombre' existe y no está vacío.
 */
fun buildQueryLotesReservados(orderBy: String, direction: String): String {
    val fieldPath = when (orderBy) {
        "booked" -> "booked.cliNombre"
        "dateBooked" -> "dateBooked"
        else -> "booked.cliNombre"
    }

    val orderDirection = if (direction.uppercase() == "ASCENDING") "ASCENDING" else "DESCENDING"
    val filterPath = "booked.cliNombre"

    val query = """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "lote" }],
            "where": {
                "fieldFilter": {
                    "field": { "fieldPath": "$filterPath" },
                    "op": "GREATER_THAN",
                    "value": { "stringValue": "" }
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "$fieldPath" }, "direction": "$orderDirection" }
            ]
        }
    }
    """.trimIndent()

    // El logging sigue siendo útil fuera del JSON.
    println("🛠️ [QUERY RESERVAS] Generada (GREATER_THAN): Ordenar por $fieldPath ($orderDirection)")
    println("🛠️ [QUERY RESERVAS] JSON: $query")

    return query
}

fun buildPostBodyForComanda(comanda: Comanda): String {
    val jsonBody = buildJsonObject {
        putJsonObject("fields") {

            putJsonObject("numeroDeComanda") {
                put("integerValue", comanda.numeroDeComanda.toString())
            }

            putJsonObject("numberLoteComanda") {
                put("stringValue", comanda.numberLoteComanda)
            }

            putJsonObject("descriptionLoteComanda") {
                put("stringValue", comanda.descriptionLoteComanda)
            }

            putJsonObject("dateBookedComanda") {
                if (comanda.dateBookedComanda != null) {
                    put("timestampValue", comanda.dateBookedComanda.toString())
                } else {
                    put("nullValue", JsonNull)
                }
            }

            putJsonObject("totalWeightComanda") {
                put("stringValue", comanda.totalWeightComanda)
            }

            // Cliente reservado
            if (comanda.bookedClientComanda != null) {
                putJsonObject("bookedClientComanda") {
                    putJsonObject("mapValue") {
                        putJsonObject("fields") {
                            putJsonObject("cliNombre") {
                                put("stringValue", comanda.bookedClientComanda.cliNombre)
                            }
                            putJsonObject("cliObservaciones") {
                                put("stringValue", comanda.bookedClientComanda.cliObservaciones)
                            }
                        }
                    }
                }
            } else {
                putJsonObject("bookedClientComanda") {
                    put("nullValue", JsonNull)
                }
            }

            putJsonObject("remarkComanda") {
                put("stringValue", comanda.remarkComanda)
            }

            putJsonObject("fueVendidoComanda") {
                put("booleanValue", comanda.fueVendidoComanda)
            }
        }
    }.toString()

    return jsonBody
}
fun buildQueryPendingComandasByClient(clientName: String): String {
    return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "comanda" }],
            "where": {
                "compositeFilter": {
                    "op": "AND",
                    "filters": [
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "bookedClientComanda.cliNombre" },
                                "op": "EQUAL",
                                "value": { "stringValue": "$clientName" }
                            }
                        },
                        {
                            "fieldFilter": {
                                "field": { "fieldPath": "fueVendidoComanda" },
                                "op": "EQUAL",
                                "value": { "booleanValue": false }
                            }
                        }
                    ]
                }
            },
            "orderBy": [
                { "field": { "fieldPath": "dateBookedComanda" }, "direction": "ASCENDING" }
            ]
        }
    }
    """.trimIndent()
}

