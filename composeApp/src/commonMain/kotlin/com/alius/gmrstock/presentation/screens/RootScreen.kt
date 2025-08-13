package com.alius.gmrstock.presentation.screens

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.bottombar.BottomBarColors
import com.alius.gmrstock.bottombar.BottomBarScreen
import com.alius.gmrstock.core.LocalDatabaseUrl
import com.alius.gmrstock.core.LocalFirebaseConfig
import com.alius.gmrstock.data.firebase.FirebaseDbConfig
import com.alius.gmrstock.data.getAuthRepository
import com.alius.gmrstock.domain.model.User

class RootScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = getAuthRepository()

        var checkingUser by remember { mutableStateOf(true) }
        var user by remember { mutableStateOf<User?>(null) }

        var selectedDbUrl by remember { mutableStateOf<String?>(null) }
        var selectedFirebaseConfig by remember { mutableStateOf<FirebaseDbConfig?>(null) }

        LaunchedEffect(Unit) {
            user = authRepository.getCurrentUser()
            checkingUser = false
        }

        when {
            checkingUser -> {
                CircularProgressIndicator()
            }
            user == null -> {
                navigator.replace(LoginScreen(authRepository))
            }
            selectedDbUrl == null || selectedFirebaseConfig == null -> {
                DatabaseSelectionScreen { url, firebaseConfig ->
                    selectedDbUrl = url
                    selectedFirebaseConfig = firebaseConfig
                }.Content()
            }
            else -> {
                CompositionLocalProvider(
                    LocalDatabaseUrl provides selectedDbUrl!!,
                    LocalFirebaseConfig provides selectedFirebaseConfig!!
                ) {
                    BottomBarScreen(
                        user = user!!,
                        authRepository = authRepository,
                        colors = BottomBarColors(),
                        onChangeDatabase = {
                            selectedDbUrl = null
                            selectedFirebaseConfig = null
                        }
                    ).Content()
                }
            }
        }
    }
}
