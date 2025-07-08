package com.example.notey.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.notey.ui.folders.FoldersScreen
import com.example.notey.ui.settings.SettingsScreen
import com.example.notey.ui.shared.HomeScreen
import com.example.notey.ui.shared.SharedScreen
import com.example.notey.ui.trash.TrashScreen

@Composable
fun MainContent(
    selectedTab: Int,
    sidebarWidth: Dp,
    modifier: Modifier = Modifier
) {
    when (selectedTab) {
        1 -> HomeScreen(modifier)
        2 -> SharedScreen(modifier)
        3 -> FoldersScreen(modifier)
        4 -> TrashScreen(modifier)
        5 -> SettingsScreen(modifier)
    }
}