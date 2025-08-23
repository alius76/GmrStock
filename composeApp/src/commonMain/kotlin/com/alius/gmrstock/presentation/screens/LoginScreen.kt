package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstock.bottombar.BottomBarColors
import com.alius.gmrstock.bottombar.BottomBarScreen
import com.alius.gmrstock.data.AuthRepository
import com.alius.gmrstock.presentation.screens.logic.LoginScreenLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginScreen(
    private val authRepository: AuthRepository,
    private val colors: BottomBarColors = BottomBarColors() // Recibe los colores para consistencia
) : Screen {

    @Composable
    override fun Content() {
        val logic = remember { LoginScreenLogic(authRepository) }
        val navigator = LocalNavigator.currentOrThrow

        val email by logic.email.collectAsState()
        val password by logic.password.collectAsState()
        val isLoading by logic.isLoading.collectAsState()
        val errorMessage by logic.errorMessage.collectAsState()
        val user by logic.user.collectAsState()


        LaunchedEffect(user) {
            user?.let {
                navigator.replace(RootScreen())
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.topBarBackground.copy(alpha = 0.1f) // Un fondo suave relacionado con el topBar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GMR Stock",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF029083),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Iniciar Sesión / Registro",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topBarBackground,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = logic::updateEmail,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colors.topBarBackground,
                        focusedLabelColor = colors.topBarBackground
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = logic::updatePassword,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colors.topBarBackground,
                        focusedLabelColor = colors.topBarBackground
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                logic.login()
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(backgroundColor = colors.topBarBackground),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Iniciar sesión", fontSize = 16.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                logic.register()
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(backgroundColor = colors.topBarBackground.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Registrar", fontSize = 16.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Version: 1.0.0",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

            }
        }
    }
}