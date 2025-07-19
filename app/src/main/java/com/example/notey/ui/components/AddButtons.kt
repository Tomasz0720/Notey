package com.example.notey.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notey.R

@Composable
fun AddButtons(modifier: Modifier = Modifier) {
    val showSubMenu = remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Toggle (plus) Button
            Button(
                onClick = { showSubMenu.value = !showSubMenu.value },
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDEDED)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add",
                    tint = Color(0xFF2D2D2D),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom Main Button
            Button(
                onClick = { /* Bottom button action */ },
                modifier = Modifier
                    .height(60.dp)
                    .widthIn(min = 140.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDEDED)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.scribble),
                        contentDescription = null,
                        tint = Color(0xFF2D2D2D),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "New Note",
                        color = Color(0xFF2D2D2D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Overlayed submenu box — 20dp above bottom button, 20dp to right of plus button
        if (showSubMenu.value) {
            Surface(
                color = Color(0xFFEDEDED),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .absoluteOffset(x = -80.dp, y = (-80).dp) // Y offset positions above the button
                    .wrapContentWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(25.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SubmenuItem(
                        icon = R.drawable.stylus_scribble,
                        label = "New Canvas",
                        onClick = { /* TODO */ }
                    )
                    SubmenuItem(
                        icon = R.drawable.new_folder,
                        label = "New Folder",
                        onClick = { /* TODO */ }
                    )
                    SubmenuItem(
                        icon = R.drawable.document,
                        label = "Import File",
                        onClick = { /* TODO */ }
                    )
                    SubmenuItem(
                        icon = R.drawable.image,
                        label = "Import Photo",
                        onClick = { /* TODO */ }
                    )
                    SubmenuItem(
                        icon = R.drawable.scan,
                        label = "Scan Document",
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun SubmenuItem(icon: Int, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.wrapContentWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color(0xFF2D2D2D),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = Color(0xFF2D2D2D),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}