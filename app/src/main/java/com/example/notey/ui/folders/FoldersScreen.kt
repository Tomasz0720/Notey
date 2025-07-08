package com.example.notey.ui.folders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FoldersScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(), // Make the Box fill the whole screen
        contentAlignment = Alignment.Center // Align content to the center
    ) {
        Text("folders") // Display the text "folders"
    }
    // Your home screen content here
}
