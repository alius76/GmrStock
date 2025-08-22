package com.alius.gmrstock.bottombar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.presentation.screens.ProcessScreenContent


class ProcessTab(
    private val user: User,
    private val databaseUrl: String
) : Tab {

    override val key: String = "ProcessTab_${user.id}"

    override val options: TabOptions
        @Composable
        get() {
            val icon: VectorPainter = rememberVectorPainter(Icons.Default.Autorenew)
            return remember {
                TabOptions(
                    index = 4u,
                    title = "WIP",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        ProcessScreenContent(user = user, databaseUrl = databaseUrl)
    }
}
