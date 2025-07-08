package com.example.notey.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notey.R


@Composable
fun AddButtons() {
    // Bottom Buttons - Right justified and properly positioned
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(
                onClick = { /* Second button action */ },
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDEDED)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = null,
                    tint = Color(0xFF2D2D2D),
                    modifier = Modifier.size(35.dp)
                )
            }

            Button(
                onClick = { /* First button action */ },
                modifier = Modifier
                    .height(65.dp)
                    .widthIn(min = 195.dp),
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
                        modifier = Modifier.size(35.dp)
                    )
                    Text(
                        text = "New Note",
                        color = Color(0xFF2D2D2D),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}