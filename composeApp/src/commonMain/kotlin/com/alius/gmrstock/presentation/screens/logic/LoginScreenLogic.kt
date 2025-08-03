package com.alius.gmrstock.presentation.screens.logic

import com.alius.gmrstock.data.AuthRepository
import com.alius.gmrstock.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenLogic(
    private val authRepository: AuthRepository
) {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        CoroutineScope(Dispatchers.Main).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val loggedUser = authRepository.login(email.value, password.value)
                if (loggedUser != null) {
                    _user.value = loggedUser
                } else {
                    _errorMessage.value = "Usuario o contraseña incorrectos"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register() {
        CoroutineScope(Dispatchers.Main).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val newUser = authRepository.register(email.value, password.value)
                if (newUser != null) {
                    _user.value = newUser
                } else {
                    _errorMessage.value = "No se pudo registrar el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
