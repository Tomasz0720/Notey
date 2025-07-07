package com.example.notey

import App
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.notey.ui.WhiteboardScreen // Import WhiteboardScreen
import com.example.notey.ui.theme.NoteyTheme // Make sure this path is correct
import androidx.appcompat.app.AppCompatActivity // Import AppCompatActivity

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request to draw behind the system bars (status bar and navigation bar).
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NoteyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Directly call the WhiteboardScreen Composable
                    App()
                    //WhiteboardScreen()
                }
            }
        }
    }

    // REMOVE these onResume and onPause overrides.
    // They are no longer needed as GLDrawingView lifecycle is managed by WhiteboardScreen.
    /*
    override fun onResume() {
        super.onResume()
        if (this::glDrawingView.isInitialized) {
            glDrawingView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::glDrawingView.isInitialized) {
            glDrawingView.onPause()
        }
    }
    */
}