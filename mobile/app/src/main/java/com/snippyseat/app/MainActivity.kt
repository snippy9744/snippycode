package com.snippyseat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.snippyseat.app.ui.SnippySeatApp
import com.snippyseat.app.ui.theme.SnippySeatTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnippySeatTheme {
                SnippySeatApp()
            }
        }
    }
}
