package com.alius.gmrstock.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.presentation.screens.ClientScreenContent


class ClientTab(
    private val user: User,
    private val databaseUrl: String
) : Tab {

    override val key: String = "ClientTab_${user.id}"

    override val options: TabOptions
        @Composable
        get() {
            val icon: VectorPainter = rememberVectorPainter(Icons.Filled.BarChart)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Ranking",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        ClientScreenContent(user = user, databaseUrl = databaseUrl)
    }
}
