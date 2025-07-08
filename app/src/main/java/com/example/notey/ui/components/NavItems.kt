package com.example.notey.ui.components

import com.example.notey.R

data class NavItem(val iconResId: Int, val label: String)

val navItems = listOf(
    NavItem(R.drawable.menu, "Menu"),
    NavItem(R.drawable.home, "Home"),
    NavItem(R.drawable.share, "Shared"),
    NavItem(R.drawable.folder, "Folders"),
    NavItem(R.drawable.trash, "Trash"),
    NavItem(R.drawable.settings, "Settings")
)