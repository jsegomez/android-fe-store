package com.jsegomez.store.features.auth.presentation.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jsegomez.store.ui.components.AppTextField
import com.jsegomez.store.ui.components.PrimaryButton
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun RegisterScreen (
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(),
){
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 40.dp, end = 24.dp, start = 24.dp),
    ) {
        Text(
            text = "Bienvenido 👋",
            style = AppTextStyles.HeadlineBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Inicia sesión para continuar",
            style = AppTextStyles.BodyLargeRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AppTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            placeholder = "example@mail.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.email.isNotBlank() && !uiState.isEmailValid,
            errorMessage = if (uiState.email.isNotBlank() && !uiState.isEmailValid) "Ingresa un correo válido" else null,
            modifier = Modifier.padding(top = 24.dp)
        )
        AppTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = "Name",
            placeholder = "John Doe",
            isPassword = false,
            isError = uiState.name.isNotBlank() && !uiState.isNameValid,
            errorMessage = if (uiState.name.isNotBlank() && !uiState.isNameValid) {
                "Favor ingrese su nombre"
            } else {
                null
            },
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Olvidaste tu contraseña?",
            style = AppTextStyles.BodyMediumSemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )

        PrimaryButton(
            text = "Iniciar sesión",
            onClick = { println("Iniciar sesión") },
            modifier = Modifier.padding(top = 24.dp),
            enabled = uiState.isFormValid
        )

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {
                    println("Registrate")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "No tienes una cuenta?",
                    style = AppTextStyles.BodyLargeMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Registrate",
                    style = AppTextStyles.BodyLargeMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}