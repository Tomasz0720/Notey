package com.example.notey

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.notey.ui.ResponsiveLayout

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    MaterialTheme {
        val windowSizeClass = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(
            activity = LocalContext.current as ComponentActivity
        )
        ResponsiveLayout(windowSizeClass)
    }
}