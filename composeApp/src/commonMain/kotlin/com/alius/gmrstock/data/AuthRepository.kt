package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User?
    suspend fun register(email: String, password: String): User?
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}

expect fun getAuthRepository(): AuthRepository