package com.example.notey.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notey.ui.components.BottomNavBar
import com.example.notey.ui.components.Sidebar

@Composable
fun ResponsiveLayout(windowSizeClass: WindowSizeClass) {
    var selectedTab by remember { mutableStateOf(1) }

    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium) {
        // Tablet/Desktop Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(20.dp)
        ) {
            Sidebar(
                modifier = Modifier
                    .fillMaxHeight(),
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.width(20.dp)) // Space between sidebar and content

            MainContent(
                selectedTab = selectedTab,
                sidebarWidth = 240.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    } else {
        // Phone Layout
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedTab,
                    onItemSelected = { selectedTab = it }
                )
            }
        ) { scaffoldPadding ->
            MainContent(
                selectedTab = selectedTab,
                sidebarWidth = 0.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(20.dp)
            )
        }
    }
}