package com.example.notey.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    onItemSelected: (Int) -> Unit,
    selectedIndex: Int
) {
    val menuIcon = navItems[0]
    val groupedIcons = navItems.subList(1, navItems.size - 1)
    val settingsIcon = navItems.last()

    var collapsed by remember { mutableStateOf(false)}
    val sidebarWidth by animateDpAsState(targetValue = if (collapsed) 80.dp else 240.dp)

    Box(
        modifier = modifier
            .width(sidebarWidth)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFEDEDED))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top section (menu)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { collapsed = !collapsed }
                ) {
                    Icon(
                        painter = painterResource(id = menuIcon.iconResId),
                        contentDescription = menuIcon.label,
                        modifier = Modifier.size(35.dp),
                        tint = Color(0xFF2D2D2D)
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                // Middle group
                Column(
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    groupedIcons.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemSelected(index + 1) }
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.label,
                                modifier = Modifier.size(35.dp),
                                tint = if (selectedIndex == index + 1) Color(0xFF777777) else Color(0xFF2D2D2D)
                            )

                            if (!collapsed) {
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedIndex == index + 1) Color(0xFF777777) else Color(0xFF2D2D2D)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom section (settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemSelected(navItems.lastIndex) }
            ) {
                Icon(
                    painter = painterResource(id = settingsIcon.iconResId),
                    contentDescription = settingsIcon.label,
                    modifier = Modifier.size(35.dp),
                    tint = if (selectedIndex == navItems.lastIndex) Color(0xFF777777) else Color(0xFF2D2D2D)
                )
            }
        }
    }
}