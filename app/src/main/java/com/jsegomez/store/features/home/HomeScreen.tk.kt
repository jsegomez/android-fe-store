package com.jsegomez.store.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToProductRegister: () -> Unit,
    onNavigateToProductList: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = "Home Screen", style = AppTextStyles.BodyXLargeMedium)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onNavigateToProductRegister, modifier = Modifier.width(210.dp)) { Text(text = "Registrar producto") }
        Button(onClick = onNavigateToProductList, modifier = Modifier.width(210.dp)) { Text(text = "Ver todos los productos") }
    }
}
