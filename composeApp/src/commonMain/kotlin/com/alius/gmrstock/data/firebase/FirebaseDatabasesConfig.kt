package com.alius.gmrstock.data.firebase

data class FirebaseDbConfig(
    val projectId: String,
    val appId: String,
    val apiKey: String
)

object FirebaseDatabasesConfig {
    val DATABASE_1 = FirebaseDbConfig(
        projectId = "gmrstock",
        appId = "1:384550126211:android:d532a12e79a55ff1e4d1b6",
        apiKey = "AIzaSyAadbArjVqNU2rIw-m7Rmk-K2Oj_AlphbM"
    )

    val DATABASE_2 = FirebaseDbConfig(
        projectId = "prueba-af1e8",
        appId = "1:579598910451:android:ee7ed3b18a7eb6d5cbc886",
        apiKey = "AIzaSyCKYTWeXtd8JGWpvDFY_DW21rMc3_RrzrM"
    )
}