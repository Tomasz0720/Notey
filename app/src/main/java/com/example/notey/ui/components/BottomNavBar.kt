package com.example.notey.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(onItemSelected: (Int) -> Unit, selectedIndex: Int) {
    NavigationBar {
        navItems.subList(1, navItems.size).forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(id = item.iconResId),
                        contentDescription = null
                    )
                },
                label = {},
                selected = selectedIndex == index + 1,
                onClick = { onItemSelected(index + 1) }
            )
        }
    }
}