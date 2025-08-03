package com.alius.gmrstock.data


import com.alius.gmrstock.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    override suspend fun register(email: String, password: String): User? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: return null
        user.sendEmailVerification().await()

        // Lanzar excepción para avisar que debe verificar su email antes de login
        throw Exception("Se ha enviado un correo de verificación. Verifica tu email antes de iniciar sesión.")
    }

    override suspend fun login(email: String, password: String): User? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: return null

        return if (user.isEmailVerified) {
            User(user.uid, user.email ?: "")
        } else {
            auth.signOut()
            null
        }
    }

    override suspend fun getCurrentUser(): User? {
        val user = auth.currentUser
        return if (user != null && user.isEmailVerified) {
            User(user.uid, user.email ?: "")
        } else {
            null
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}

actual fun getAuthRepository(): AuthRepository = AuthRepositoryImpl()
