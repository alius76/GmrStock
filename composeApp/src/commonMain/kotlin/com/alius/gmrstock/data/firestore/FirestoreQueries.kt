package com.alius.gmrstock.data.firestore

import kotlinx.datetime.Instant

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
        "from": [{"collectionId": "lotes"}],
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


