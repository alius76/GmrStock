package com.alius.gmrstock.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Devuelve un objeto que represente la imagen en la plataforma
expect fun loadPlatformImage(fileName: String): Any?

// Composable multiplataforma para mostrar la imagen
@Composable
expect fun PlatformImageComposable(image: Any?, modifier: Modifier)