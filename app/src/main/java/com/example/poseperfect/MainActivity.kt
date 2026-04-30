package com.example.poseperfect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.poseperfect.presentation.screen.CameraScreen
import com.example.poseperfect.presentation.theme.PosePerfectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PosePerfectTheme {
                CameraScreen()
            }
        }
    }
}

