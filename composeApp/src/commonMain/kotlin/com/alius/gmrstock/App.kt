package com.alius.gmrstock

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.alius.gmrstock.presentation.screens.RootScreen
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@Composable
fun App() {
    androidx.compose.material.MaterialTheme {
        Surface {
            Navigator(RootScreen())
            Napier.base(DebugAntilog())
        }
    }
}
