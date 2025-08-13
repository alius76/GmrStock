package com.alius.gmrstock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VentaBigbag(
    val ventaBbNumber: String = "",
    var ventaBbWeight: String = ""

)