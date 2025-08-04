import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.User
import com.alius.gmrstock.data.getLoteRepository
import kotlinx.coroutines.launch

class HomeScreenContent(private val user: User) : Screen {

    @Composable
    override fun Content() {
        val loteRepository = remember { getLoteRepository() }

        var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var addingLote by remember { mutableStateOf(false) }
        var addResultMessage by remember { mutableStateOf<String?>(null) }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    lotes = loteRepository.listarLotes("")
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Error desconocido"
                } finally {
                    isLoading = false
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "¡Bienvenido, ${user.email}!", fontSize = 24.sp)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        addingLote = true
                        addResultMessage = null
                        try {
                            loteRepository.agregarLoteConBigBags()
                            addResultMessage = "✅ Lote agregado con éxito"
                            // Opcional: refrescar la lista
                            lotes = loteRepository.listarLotes("")
                        } catch (e: Exception) {
                            addResultMessage = "❌ Error al agregar lote: ${e.message}"
                        } finally {
                            addingLote = false
                        }
                    }
                },
                enabled = !addingLote,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (addingLote) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Agregando lote...")
                } else {
                    Text("Agregar lote con BigBags")
                }
            }

            Spacer(Modifier.height(16.dp))

            addResultMessage?.let {
                Text(text = it)
                Spacer(Modifier.height(16.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(text = "Error: $errorMessage", color = MaterialTheme.colors.error)
            } else if (lotes.isEmpty()) {
                Text(text = "No se encontraron lotes.")
            } else {
                LazyColumn {
                    items(lotes) { lote ->
                        LoteItem(lote)
                        Divider()
                    }
                }
            }
        }
    }

    @Composable
    private fun LoteItem(lote: LoteModel) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Número: ${lote.number}", fontSize = 18.sp)
            Text(text = "Descripción: ${lote.description}")
            Text(text = "Ubicación: ${lote.location}")
        }
    }
}
