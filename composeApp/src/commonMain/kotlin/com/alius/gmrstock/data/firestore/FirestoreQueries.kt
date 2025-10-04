package com.alius.gmrstock.data.firestore

import com.alius.gmrstock.domain.model.Cliente
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.plus
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
 * Construye el cuerpo JSON necesario para actualizar el campo `booked`
 * en Firestore usando el método PATCH.
 */
fun buildPatchBodyForBooked(
    cliente: Cliente?,
    dateBooked: Instant?
): String {
    return buildJsonObject {
        putJsonObject("fields") {
            // booked (objeto Cliente)
            if (cliente != null) {
                putJsonObject("booked") {
                    putJsonObject("mapValue") {
                        putJsonObject("fields") {
                            putJsonObject("cliNombre") {
                                put("stringValue", cliente.cliNombre)
                            }
                            putJsonObject("cliObservaciones") {
                                put("stringValue", cliente.cliObservaciones)
                            }
                        }
                    }
                }
            }
            // Si cliente es null, NO incluimos booked en 'fields' → Firestore eliminará si está en updateMask

            // dateBooked (Instant -> timestampValue)
            if (dateBooked != null) {
                putJsonObject("dateBooked") {
                    put("timestampValue", dateBooked.toString())
                }
            }
            // Si dateBooked es null, NO lo incluimos → Firestore eliminará si está en updateMask
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