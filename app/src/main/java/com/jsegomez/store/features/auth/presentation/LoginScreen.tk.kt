package com.jsegomez.store.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    }
}