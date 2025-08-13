package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.data.FirestoreUrls
import com.alius.gmrstock.data.firebase.FirebaseDatabasesConfig
import com.alius.gmrstock.data.firebase.FirebaseDbConfig

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String, FirebaseDbConfig) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecciona la base de datos",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onDatabaseSelected(FirestoreUrls.DB1_URL, FirebaseDatabasesConfig.DATABASE_1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Base de datos 1")
                }

                Button(
                    onClick = { onDatabaseSelected(FirestoreUrls.DB2_URL, FirebaseDatabasesConfig.DATABASE_2) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Base de datos 2")
                }
            }
        }
    }
}
