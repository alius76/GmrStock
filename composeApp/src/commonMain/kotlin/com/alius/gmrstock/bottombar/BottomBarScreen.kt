package com.alius.gmrstock.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.alius.gmrstock.data.AuthRepository
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.presentation.screens.LoginScreen
import com.alius.gmrstock.core.LocalDatabaseUrl
import com.alius.gmrstock.data.FirestoreUrls
import kotlinx.coroutines.launch
import com.alius.gmrstock.getPlatform
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import cafe.adriel.voyager.navigator.internal.BackHandler

@Serializable
class BottomBarScreen(
    private val user: User,
    @Transient private val colors: BottomBarColors = BottomBarColors(),
    @Transient private val authRepository: AuthRepository? = null,
    @Transient private val onChangeDatabase: (() -> Unit)? = null
) : Screen, JavaSerializable {

    // 🔑 Forzamos una key constante para que no intente recuperar estados antiguos "sucios"
    override val key: String = "bottom_bar_main"

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val parentNavigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        var homeRefreshKey by remember { mutableStateOf(0) }
        val databaseUrl = LocalDatabaseUrl.current
        val platform = getPlatform()

        val identificadorFabrica = when (databaseUrl) {
            FirestoreUrls.DB1_URL -> "P07"
            FirestoreUrls.DB2_URL -> "P08"
            else -> "Desconocida"
        }

        // 🛡️ IMPORTANTE: Creamos las pestañas AQUÍ adentro.
        // Al no estar en el constructor de la clase ni como propiedades,
        // Voyager NO intentará guardarlas en el disco de Android/iOS.
        val homeTab = remember(user.id, databaseUrl, homeRefreshKey) {
            HomeTab(
                user = user,
                onChangeDatabase = { onChangeDatabase?.invoke() },
                onLogoutClick = {
                    coroutineScope.launch {
                        authRepository?.logout()
                        parentNavigator.replace(LoginScreen(authRepository!!))
                    }
                }
            )
        }
        val clientTab = remember(user.id, databaseUrl) { ClientTab(user, databaseUrl) }
        val batchTab = remember(user.id, databaseUrl) { BatchTab(user, databaseUrl) }
        val processTab = remember(user.id, databaseUrl) { ProcessTab(user, databaseUrl) }
        val transferTab = remember(user.id, databaseUrl) { TransferTab(user, databaseUrl) }

        TabNavigator(homeTab) { tabNavigator ->

            BackHandler(enabled = tabNavigator.current.key != homeTab.key) {
                tabNavigator.current = homeTab
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        backgroundColor = colors.topBarBackground,
                        contentColor = colors.topBarContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp + if (platform.isMobile) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp),
                        title = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (platform.isMobile) 28.dp else 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "GMR Stock - $identificadorFabrica",
                                    fontSize = 24.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = colors.topBarContent
                                )
                                Text(
                                    text = "Gestión de stock en tiempo real",
                                    fontSize = 12.sp,
                                    color = colors.topBarContent.copy(alpha = 0.8f)
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
                            icon = { Icon(Icons.Filled.Dashboard, null) },
                            label = { Text(homeTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == processTab.key,
                            onClick = { tabNavigator.current = processTab },
                            icon = { Icon(Icons.Filled.Autorenew, null) },
                            label = { Text(processTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == batchTab.key,
                            onClick = { tabNavigator.current = batchTab },
                            icon = { Icon(Icons.Outlined.ShoppingBag, null) },
                            label = { Text(batchTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == transferTab.key,
                            onClick = { tabNavigator.current = transferTab },
                            icon = { Icon(Icons.Default.LocalShipping, null) },
                            label = { Text(transferTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == clientTab.key,
                            onClick = { tabNavigator.current = clientTab },
                            icon = { Icon(Icons.Filled.BarChart, null) },
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