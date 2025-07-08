import NavItem
import com.example.notey.R
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyStaggeredGridState
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.res.painterResource
import kotlin.random.Random
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import java.time.format.TextStyle
import kotlin.text.compareTo
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.platform.LocalDensity
import kotlin.text.compareTo

@Composable
fun ResponsiveLayout(windowSizeClass: WindowSizeClass) {
    var selectedTab by remember { mutableStateOf(0) }

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

data class NavItem(val iconResId: Int, val label: String)
val navItems = listOf(
    NavItem(R.drawable.menu, "Menu"),
    NavItem(R.drawable.home, "Home"),
    NavItem(R.drawable.share, "Shared"),
    NavItem(R.drawable.folder, "Folders"),
    NavItem(R.drawable.trash, "Trash"),
    NavItem(R.drawable.settings, "Settings")
)

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

@Composable
fun MainContent(
    selectedTab: Int,
    sidebarWidth: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Text(
                text = "Welcome, User",
                fontSize = 40.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D2D2D),
            )

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFFEDEDED))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.find),
                        contentDescription = null,
                        tint = Color(0xFF2D2D2D),
                        modifier = Modifier.size(35.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    var searchQuery by remember { mutableStateOf("") }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF2D2D2D),
                            fontSize = 18.sp,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search Notes",
                                        color = Color(0xFF2D2D2D),
                                        fontSize = 18.sp,
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center info
            Text(
                text = "Current Tab: ${navItems[selectedTab].label}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

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
}




@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    MaterialTheme {
        val windowSizeClass = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(
            activity = LocalContext.current as ComponentActivity
        )
        ResponsiveLayout(windowSizeClass)
    }
}