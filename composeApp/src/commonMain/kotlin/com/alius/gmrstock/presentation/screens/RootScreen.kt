package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.bottombar.BottomBarColors
import com.alius.gmrstock.bottombar.BottomBarScreen
import com.alius.gmrstock.data.getAuthRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.getPlatform

class RootScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val platform = getPlatform()

        if (!platform.isAuthSupported) {
            UnsupportedPlatformScreen(platform.name)
            return
        }

        val authRepository = getAuthRepository()
        var checkingUser by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            val user: User? = authRepository.getCurrentUser()
            checkingUser = false
            if (user != null) {
                navigator.replace(BottomBarScreen(user, BottomBarColors(),authRepository, ))
            } else {
                navigator.replace(LoginScreen(authRepository))
            }
        }

        if (checkingUser) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun UnsupportedPlatformScreen(platformName: String) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "La aplicación no está disponible en $platformName.",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text("Por favor, utiliza un dispositivo Android o iOS.")
            }
        }
    }

}