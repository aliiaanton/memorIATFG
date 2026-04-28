package com.memoria.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.memoria.app.ui.MemoriaApp
import com.memoria.app.ui.theme.MemoriaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoriaTheme {
                MemoriaApp()
            }
        }
    }
}

