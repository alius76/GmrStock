package com.alius.gmrstock

import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GMR Stock v1.1.0 build 21",
        icon = painterResource("icon.png"),
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            width = 800.dp,
            height = 900.dp
        )
    ) {
        window.minimumSize = java.awt.Dimension(500, 700)
        App()
    }
}