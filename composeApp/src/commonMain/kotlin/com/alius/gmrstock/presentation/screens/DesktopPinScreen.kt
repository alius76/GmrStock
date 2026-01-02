package com.alius.gmrstock.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstock.ui.theme.PrimaryColor


@Composable
fun DesktopFullPinScreen(
    onSuccess: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val expectedPin = "2023"

    // Focus automático al abrir la pantalla
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    val textFieldColors = TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = PrimaryColor,
        unfocusedIndicatorColor = PrimaryColor,
        cursorColor = PrimaryColor
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Título grande
        Text(
            text = "GMR Stock",
            fontSize = 60.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = PrimaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subtítulo
        Text(
            text = "Gestión de stock en tiempo real",
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 58.dp)
        )

        // Caja de texto de línea inferior
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            if (pinInput.isEmpty()) {
                Text(
                    text = "Ingrese PIN de 4 dígitos",
                    color = PrimaryColor.copy(alpha = 0.4f),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }

            BasicTextField(
                value = pinInput,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        pinInput = newValue
                        errorMessage = null
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = PrimaryColor,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }

        // Línea inferior del TextField
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(2.dp)
                .background(PrimaryColor)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (pinInput == expectedPin) {
                    onSuccess()
                } else {
                    errorMessage = "PIN incorrecto"
                    pinInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor)
        ) {
            Text("Validar PIN", fontSize = 20.sp, color = Color.White)
        }
    }
}
