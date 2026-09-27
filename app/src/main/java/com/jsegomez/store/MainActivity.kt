package com.jsegomez.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.jsegomez.store.navigation.NavGraph
import com.jsegomez.store.ui.theme.StoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    NavGraph( modifier = Modifier.padding(innerPadding) )
                }
            }
        }
    }
}