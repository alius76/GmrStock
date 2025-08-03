package com.alius.gmrstock.data


import com.alius.gmrstock.domain.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class AuthRepositoryImpl : AuthRepository {

    override suspend fun register(email: String, password: String): User? {
        val result = Firebase.auth.createUserWithEmailAndPassword(email, password)
        val user = result.user ?: return null

        user.sendEmailVerification()

        // Lanzar excepción para avisar que debe verificar su email antes de login
        throw Exception("Se ha enviado un correo de verificación. Verifica tu email antes de iniciar sesión.")
    }

    override suspend fun login(email: String, password: String): User? {
        val result = Firebase.auth.signInWithEmailAndPassword(email, password)
        val user = result.user ?: return null

        return if (user.isEmailVerified) {
            User(user.uid, user.email ?: "")
        } else {
            Firebase.auth.signOut()
            null
        }
    }

    override suspend fun getCurrentUser(): User? {
        val user = Firebase.auth.currentUser
        return if (user != null && user.isEmailVerified) {
            User(user.uid, user.email ?: "")
        } else {
            null
        }
    }

    override suspend fun logout() {
        Firebase.auth.signOut()
    }
}

actual fun getAuthRepository(): AuthRepository = AuthRepositoryImpl()
