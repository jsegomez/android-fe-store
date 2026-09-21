package com.jsegomez.store.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun LoginScreen(modifier: Modifier = Modifier){
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Text(text = "Bienvenido", style = AppTextStyles.BodyXLargeMedium)
    }
}