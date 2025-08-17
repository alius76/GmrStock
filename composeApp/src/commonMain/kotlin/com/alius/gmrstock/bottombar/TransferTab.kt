package com.alius.gmrstock.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.presentation.screens.TransferScreenContent

class TransferTab(
    private val user: User,
    private val databaseUrl: String
) : Tab {

    override val key: String = "TransferTab_${user.id}"

    override val options: TabOptions
        @Composable
        get() {
            val icon: VectorPainter = rememberVectorPainter(Icons.Filled.EuroSymbol)
            return remember {
                TabOptions(
                    index = 5u,
                    title = "Ventas",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        TransferScreenContent(user = user, databaseUrl = databaseUrl)
    }
}
