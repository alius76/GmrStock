package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import com.alius.gmrstock.data.getLoteRepository
import com.alius.gmrstock.domain.model.User

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientScreenContent(user: User, databaseUrl: String) {
    val loteRepository = remember(databaseUrl) { getLoteRepository(databaseUrl) }

}