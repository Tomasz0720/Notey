package com.example.notey.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.notey.R
import com.example.notey.ui.theme.AppColors

@Composable
fun FolderCards(title: String, items: Int, color: Color){
    Card(
        modifier = Modifier
            .width(206.dp)
            .height(156.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)), // Lighter version for card body
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            // --- Bottom Petal (50% transparent, slightly offset) ---
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.folderpetal), // Your petal SVG
                contentDescription = null, // Decorative, no content description needed
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Adjust width to match your design
                    .align(Alignment.TopEnd) // Align to the top-right
                    .offset(x = (-8).dp, y = (-4).dp) // Offset it slightly from the top-right
                    .alpha(0.5f), // 50% transparency
                colorFilter = ColorFilter.tint(color) // Tint with the folder's main color
            )

            // --- Top Petal (100% opaque, in front) ---
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.folderpetal), // Your petal SVG
                contentDescription = null, // Decorative
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Adjust width to match your design
                    .align(Alignment.TopEnd), // Align to the top-right (no offset here)
                colorFilter = ColorFilter.tint(color) // Tint with the folder's main color
            )

            // --- Content (text and icon), aligned below the petals ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .padding(top = 24.dp), // Add padding to push content below the petal area
                horizontalAlignment = Alignment.CenterHorizontally, // Adjust as per your layout
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.folderwidget),
                    contentDescription = null // or provide a string description
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$items Items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
        }
    }
}

//@Composable @Preview
//fun SideMenu(){
//    val configuration = LocalConfiguration.current
//    val screenWidthDp = configuration.screenWidthDp.dp // Get the screen width in dp
//    val screenHeightDp = configuration.screenHeightDp.dp // Get the screen height in dp
//
//    val density = LocalDensity.current // Get the current screen density
//    val screenWidthPx = with(density) { screenWidthDp.toPx() } // Convert dp to pixels
//    val screenHeightPx = with(density) { screenHeightDp.toPx() } // Convert dp to pixels
//
//    val menuWidth = if(screenWidthDp > 600.dp) 300.dp else screenWidthDp * 0.8f // Set menu width to 300dp or 80% of screen width for smaller screens
//    val menuHeight = if (screenHeightDp > 800.dp) 400.dp else screenHeightDp * 0.6f // Set menu height to 400dp or 60% of screen height for smaller screens
//
//
//    Box( // Use Box to create a container for the side menu
//        modifier = Modifier
//            .width(menuWidth)
//            .height(menuHeight)
//            .background(Color(0xFFF1F7FF))
//            .shadow(
//                elevation = 5.dp,
//                shape = RoundedCornerShape(10.dp),
//                clip = false
//            )
//    )
//
//}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable @Preview
fun MainScreenLayout() {
    val context = LocalContext.current

    val windowSizeClass = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(
        activity = context as ComponentActivity
    )
    val showPermanentNavigationDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    // Using PermanentNavigationDrawer for the always-open menu in your concept
    PermanentNavigationDrawer(
        drawerContent = {
            // Content of left-side menu
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(240.dp) // Fixed width for the menu
                    .background(Color(0xFFF0F5F9)) // Light grey background like your image
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Hamburger Icon
                IconButton(onClick = { /* if modal: drawerState.close() */ }, modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.menu), contentDescription = "Menu")
                }

                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.home), contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true, // Highlight if it's the current selected item
                    onClick = { /* Navigate to Home */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.share), contentDescription = "Shared") },
                    label = { Text("Shared") },
                    selected = false,
                    onClick = { /* Navigate to Shared */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.folder), contentDescription = "Folders") },
                    label = { Text("Folders") },
                    selected = false,
                    onClick = { /* Navigate to Folders */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.trash), contentDescription = "Trash") },
                    label = { Text("Trash") },
                    selected = false,
                    onClick = { /* Navigate to Trash */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Spacer to push the settings icon to the bottom
                Spacer(Modifier.weight(1f))

                // Settings icon at the bottom
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.settings), contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { /* Open Settings */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        // This is the main content area (Welcome, User, and your folder cards)
        Scaffold(
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    ExtendedFloatingActionButton(
                        onClick = { /* Handle New Note click */ },
                        text = { Text("New Note") },
                        icon = { Icon(painterResource(id = R.drawable.scribble), contentDescription = "New Note") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingActionButton(onClick = { /* Handle Add Folder click */ }) {
                        Icon(painterResource(id = R.drawable.add), contentDescription = "New")
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply Scaffold's padding
                    .padding(start = if (showPermanentNavigationDrawer) 0.dp else 16.dp) // Adjust padding if modal
            ) {
                // "Welcome, User" text
                Text(
                    text = "Welcome, User",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 24.dp)
                )

                // Your Folder Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                    // Consider using a LazyRow if you expect many folders horizontally
                ) {
                    FolderCards(title = "Calculus", items = 8, color = Color(0xFFE57373)) // Reddish
                    FolderCards(title = "Linear Algebra", items = 15, color = Color(0xFF81C784)) // Greenish
                    FolderCards(title = "English", items = 2, color = Color(0xFF64B5F6)) // Bluish
                }

                // Add more content as needed
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 700) // Simulate a tablet/desktop screen
@Composable
fun MainScreenLayoutPreview() {
    MaterialTheme {
        MainScreenLayout()
    }
}