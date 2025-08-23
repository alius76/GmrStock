package com.alius.gmrstock.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        val databaseUrl = LocalDatabaseUrl.current

        // Ahora pasamos también el callback de logout al HomeTab
        val homeTab = remember {
            HomeTab(
                user = user,
                onChangeDatabase = onChangeDatabase,
                onLogoutClick = {
                    coroutineScope.launch {
                        authRepository.logout()
                        parentNavigator.replace(LoginScreen(authRepository))
                    }
                }
            )
        }

        val clientTab = remember { ClientTab(user, databaseUrl) }
        val batchTab = remember { BatchTab(user, databaseUrl) }
        val processTab = remember { ProcessTab(user, databaseUrl) }
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
                        title = { Text("GMR Stock - ${tabNavigator.current.options.title}") },
                        backgroundColor = Color(0xFF029083),
                        contentColor = Color.White,
                        modifier = Modifier.statusBarsPadding(),
                        actions = {
                            // Solo mostrar botones en HomeTab
                            if (tabNavigator.current.key.startsWith(homeTab.key)) {
                                // Botón Swap con texto
                                Row(
                                    modifier = Modifier
                                        .clickable { onChangeDatabase() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SWAP",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Swap base de datos",
                                        tint = Color.White
                                    )
                                }
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
                            icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                            label = { Text(homeTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == clientTab.key,
                            onClick = { tabNavigator.current = clientTab },
                            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            label = { Text(clientTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == batchTab.key,
                            onClick = { tabNavigator.current = batchTab },
                            icon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = null) },
                            label = { Text(batchTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == processTab.key,
                            onClick = { tabNavigator.current = processTab },
                            icon = { Icon(Icons.Default.Autorenew, contentDescription = null) },
                            label = { Text(processTab.options.title) }
                        )

                        BottomNavigationItem(
                            selected = tabNavigator.current.key == transferTab.key,
                            onClick = { tabNavigator.current = transferTab },
                            icon = { Icon(Icons.Filled.EuroSymbol, contentDescription = null) },
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
