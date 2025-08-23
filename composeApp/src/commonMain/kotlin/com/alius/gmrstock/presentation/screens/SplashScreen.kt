package com.alius.gmrstock.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val PrimaryColor = Color(0xFF029083)

@Composable
fun SplashScreen(durationMillis: Int = 3000) {
    println("📌 [SplashScreen] Comenzando splash screen")

    // Animaciones fade-in y slide-up
    val alphaAnim = remember { Animatable(0f) }
    val offsetY = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        println("📌 [SplashScreen] Iniciando animaciones")
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Espera total del splash
        delay(durationMillis.toLong())
        println("📌 [SplashScreen] Splash terminado")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor),
        contentAlignment = Alignment.Center
    ) {
        // Contenido central animado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = offsetY.value.dp)
        ) {
            Text(
                text = "GMR Stock",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Gestión de Inventario",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                modifier = Modifier.alpha(alphaAnim.value),
                color = Color.White,
                strokeWidth = 6.dp
            )
        }

        // Texto inferior
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Desarrollado por Alejandro II",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}
