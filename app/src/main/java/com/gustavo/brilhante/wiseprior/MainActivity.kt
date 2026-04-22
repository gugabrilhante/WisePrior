package com.gustavo.brilhante.wiseprior

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.gustavo.brilhante.designsystem.theme.WisePriorTheme
import com.gustavo.brilhante.wiseprior.navigation.WisePriorNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WisePriorTheme {
                WisePriorNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
