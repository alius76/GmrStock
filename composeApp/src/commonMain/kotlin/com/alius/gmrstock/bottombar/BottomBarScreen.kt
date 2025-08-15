package com.alius.gmrstock.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.alius.gmrstock.data.AuthRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.presentation.screens.LoginScreen
import com.alius.gmrstock.core.LocalDatabaseUrl
import kotlinx.coroutines.launch

class BottomBarScreen(
    private val user: User,
    private val colors: BottomBarColors = BottomBarColors(),
    private val authRepository: AuthRepository,
    private val onChangeDatabase: () -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val parentNavigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var homeRefreshKey by remember { mutableStateOf(0) }

        // Leemos la base de datos actual del CompositionLocal
        val databaseUrl = LocalDatabaseUrl.current

        val homeTab = remember { HomeTab(user, onChangeDatabase) }
        val clientTab = remember { ClientTab(user, databaseUrl) }
        val batchTab = remember { BatchTab(user, databaseUrl) }
        val processTab = remember { ProcessTab(user) }
        val transferTab = remember { TransferTab(user, databaseUrl) }

        TabNavigator(
            homeTab,
            tabDisposable = {
                TabDisposable(
                    it,
                    listOf(
                        homeTab,
                        clientTab,
                        batchTab,
                        processTab,
                        transferTab
                    )
                )
            }
        ) { tabNavigator ->

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(tabNavigator.current.options.title) },
                        backgroundColor = Color(0xFF029083),
                        contentColor = Color.White,
                        modifier = Modifier.statusBarsPadding(),
                        actions = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    authRepository.logout()
                                    parentNavigator.replace(LoginScreen(authRepository))
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavigation(
                        backgroundColor = colors.bottomBarBackground,
                        contentColor = colors.bottomBarContent,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        BottomNavigationItem(
                            selected = tabNavigator.current.key.startsWith(homeTab.key),
                            onClick = {
                                if (tabNavigator.current.key.startsWith(homeTab.key)) {
                                    homeRefreshKey++
                                }
                                tabNavigator.current = homeTab
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text(homeTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == clientTab.key,
                            onClick = { tabNavigator.current = clientTab },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text(clientTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == batchTab.key,
                            onClick = { tabNavigator.current = batchTab },
                            icon = { Icon(Icons.Default.Polymer, contentDescription = null) },
                            label = { Text(batchTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == processTab.key,
                            onClick = { tabNavigator.current = processTab },
                            icon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                            label = { Text(processTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == transferTab.key,
                            onClick = { tabNavigator.current = transferTab },
                            icon = { Icon(Icons.Default.Train, contentDescription = null) },
                            label = { Text(transferTab.options.title) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(color = colors.bottomBarBackground)
                ) {
                    CurrentTab()
                }
            }
        }
    }
}
