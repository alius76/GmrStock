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
        val authRepository = getAuthRepository()

        var checkingUser by remember { mutableStateOf(true) }
        var user by remember { mutableStateOf<User?>(null) }
        var selectedDbUrl by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            user = authRepository.getCurrentUser()
            checkingUser = false
        }

        when {
            checkingUser -> {
                CircularProgressIndicator()
            }
            user == null -> {
                // Usuario no logueado, ir a login
                navigator.replace(LoginScreen(authRepository))
            }
            selectedDbUrl == null -> {
                // Usuario logueado pero sin DB seleccionada, mostrar selector
                DatabaseSelectionScreen { url ->
                    selectedDbUrl = url
                }.Content()
            }
            else -> {
                // Usuario logueado y DB seleccionada, mostrar BottomBarScreen pasándole databaseUrl
                BottomBarScreen(
                    user = user!!,
                    authRepository = authRepository,
                    databaseUrl = selectedDbUrl!!,
                    colors = BottomBarColors(),
                    onChangeDatabase = {
                        selectedDbUrl = null  // Esto hará que regrese a seleccionar la base de datos
                    }
                ).Content()
            }
        }
    }
}
