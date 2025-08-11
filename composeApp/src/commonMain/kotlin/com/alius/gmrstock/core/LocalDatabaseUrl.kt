package com.alius.gmrstock.core

import androidx.compose.runtime.staticCompositionLocalOf

val LocalDatabaseUrl = staticCompositionLocalOf<String> {
    error("DatabaseUrl no encontrado. Proporciónalo con CompositionLocalProvider.")
}