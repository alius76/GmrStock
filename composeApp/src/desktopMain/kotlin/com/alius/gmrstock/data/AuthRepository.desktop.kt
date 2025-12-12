package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.User
import kotlinx.coroutines.delay

/**
 * Repositorio de Autenticación de MOCK para Desktop.
 * Implementación con BYPASS ACTIVO: Siempre devuelve un usuario válido
 * al iniciar para saltar la pantalla de Login y probar el resto de la App.
 */
class DesktopAuthRepositoryImpl : AuthRepository {

    private val MOCK_EMAIL = "admin@llorensgmr.com"
    private val MOCK_PASSWORD = "llorensgmr"
    private val MOCK_UID = "desktop-user-12345"

    // ⭐️ CAMBIO CLAVE: Inicializamos currentUser con un usuario válido
    // para que getCurrentUser() no devuelva null al inicio.
    private var currentUser: User? = User(MOCK_UID, MOCK_EMAIL)

    // Simulamos un retraso para imitar la latencia de red
    private suspend fun simulateNetworkDelay() = delay(300L)

    override suspend fun login(email: String, password: String): User? {
        simulateNetworkDelay()

        if (email == MOCK_EMAIL && password == MOCK_PASSWORD) {
            val user = User(MOCK_UID, MOCK_EMAIL)
            currentUser = user
            println("✅ [Desktop Mock] Login simulado exitoso para $email.")
            return user
        } else {
            // Si el login falla, simulamos una espera y devolvemos null
            println("❌ [Desktop Mock] Error de credenciales simulado.")
            return null
        }
    }

    override suspend fun register(email: String, password: String): User? {
        // En Desktop, la funcionalidad de registro no tiene sentido sin Firebase.
        throw UnsupportedOperationException("La función de registro no está soportada en la versión Desktop.")
    }

    override suspend fun getCurrentUser(): User? {
        // ⭐️ Devuelve el usuario preconfigurado (el bypass).
        simulateNetworkDelay()
        return currentUser
    }

    override suspend fun logout() {
        simulateNetworkDelay()
        currentUser = null
        println("🚪 [Desktop Mock] Logout simulado.")
    }
}

actual fun getAuthRepository(): AuthRepository = DesktopAuthRepositoryImpl()