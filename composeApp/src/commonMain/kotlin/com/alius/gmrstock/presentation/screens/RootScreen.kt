package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.bottombar.BottomBarColors
import com.alius.gmrstock.bottombar.BottomBarScreen
import com.alius.gmrstock.core.LocalDatabaseUrl
import com.alius.gmrstock.data.getAuthRepository
import com.alius.gmrstock.domain.model.User
import kotlinx.coroutines.delay
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.alius.gmrstock.ui.theme.PrimaryColor
import com.alius.gmrstock.getPlatform


class RootScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = getAuthRepository()

        var showingSplash by remember { mutableStateOf(true) }
        var checkingUser by remember { mutableStateOf(true) }
        var user by remember { mutableStateOf<User?>(null) }
        var selectedDbUrl by remember { mutableStateOf<String?>(null) }

        // 🔐 Estado interno para controlar el acceso por PIN en Desktop
        var isDesktopPinValidated by remember { mutableStateOf(false) }

        // Mostrar splash al iniciar
        LaunchedEffect(Unit) {
            delay(2000)
            showingSplash = false
        }

        // Comprobar usuario solo después del splash
        LaunchedEffect(showingSplash) {
            if (!showingSplash) {
                user = authRepository.getCurrentUser()
                checkingUser = false
            }
        }

        when {
            showingSplash -> {
                SplashScreen()
            }

            checkingUser -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            user == null -> {
                navigator.replace(LoginScreen(authRepository))
            }

            selectedDbUrl == null -> {

                // 🔐 SOLO DESKTOP PEDIR PIN
                if (!getPlatform().isMobile && !isDesktopPinValidated) {
                    DesktopFullPinScreen(
                        onSuccess = {
                            isDesktopPinValidated = true
                        }
                    )
                    return
                }

                if (isDesktopPinValidated || getPlatform().isMobile) {
                    DatabaseSelectionScreen { url ->
                        selectedDbUrl = url
                    }.Content()
                }
            }

            else -> {
                CompositionLocalProvider(
                    LocalDatabaseUrl provides selectedDbUrl!!
                ) {
                    BottomBarScreen(
                        user = user!!,
                        authRepository = authRepository,
                        colors = BottomBarColors(),
                        onChangeDatabase = {
                            selectedDbUrl = null
                            isDesktopPinValidated = false
                        }
                    ).Content()
                }
            }
        }
    }
}
