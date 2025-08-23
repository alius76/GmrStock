package com.alius.gmrstock.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.presentation.screens.HomeScreenContent

class HomeTab(
    private val user: User,
    private val onChangeDatabase: () -> Unit,
    private val onLogoutClick: () -> Unit = {}   // ← callback logout
) : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Dashboard)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Inicio",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        HomeScreenContent(
            user = user,
            onChangeDatabase = onChangeDatabase,
            onLogoutClick = onLogoutClick
        ).Content()
    }
}
