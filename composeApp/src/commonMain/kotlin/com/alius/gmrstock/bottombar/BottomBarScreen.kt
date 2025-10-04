package com.alius.gmrstock.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.alius.gmrstock.data.FirestoreUrls
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

        val identificadorFabrica = when (databaseUrl) {
            FirestoreUrls.DB1_URL -> "P07"
            FirestoreUrls.DB2_URL -> "P08"
            else -> "Desconocida"
        }

        // Tabs
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
                    listOf(homeTab, clientTab, batchTab, processTab, transferTab)
                )
            }
        ) { tabNavigator ->

            Scaffold(
                topBar = {
                    TopAppBar(
                        backgroundColor = colors.topBarBackground,
                        contentColor = colors.topBarContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()), // un poco más alto
                        title = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 36.dp), // margen superior para alinear
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "GMR Stock - $identificadorFabrica",
                                        fontSize = 26.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = colors.topBarContent
                                    )
                                    Text(
                                        text = "Gestión de stock en tiempo real",
                                        fontSize = 14.sp,
                                        color = colors.topBarContent.copy(alpha = 0.8f)
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
                            selected = tabNavigator.current.key == processTab.key,
                            onClick = { tabNavigator.current = processTab },
                            icon = { Icon(Icons.Filled.Autorenew, contentDescription = null) },
                            label = { Text(processTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == batchTab.key,
                            onClick = { tabNavigator.current = batchTab },
                            icon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = null) },
                            label = { Text(batchTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == transferTab.key,
                            onClick = { tabNavigator.current = transferTab },
                            icon = { Icon(Icons.Default.Archive, contentDescription = null) },
                            label = { Text(transferTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == clientTab.key,
                            onClick = { tabNavigator.current = clientTab },
                            icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                            label = { Text(clientTab.options.title) }
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
