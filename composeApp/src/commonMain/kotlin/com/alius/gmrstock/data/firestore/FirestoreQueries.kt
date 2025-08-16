package com.alius.gmrstock.data.firestore

import kotlinx.datetime.Instant


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


