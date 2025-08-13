package com.alius.gmrstock.core

import androidx.compose.runtime.staticCompositionLocalOf
import com.alius.gmrstock.data.firebase.FirebaseDbConfig

val LocalFirebaseConfig = staticCompositionLocalOf<FirebaseDbConfig> {
    error("FirebaseConfig no encontrado. Proporciónalo con CompositionLocalProvider.")
}