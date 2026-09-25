package com.jsegomez.store.features.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jsegomez.store.ui.components.AppTextField
import com.jsegomez.store.ui.components.PrimaryButton
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun LoginScreen(modifier: Modifier = Modifier){
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
            value = "",
            onValueChange = {},
            label = "Email",
            placeholder = "example@mail.com",
            modifier = Modifier.padding(top = 24.dp)
        )
        AppTextField(
            value = "",
            onValueChange = {},
            label = "Password",
            placeholder = "••••••••",
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
            onClick = {},
            modifier = Modifier.padding(top = 24.dp)
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