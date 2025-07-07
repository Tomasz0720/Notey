import NavItem
import com.example.notey.R
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
import androidx.compose.ui.res.painterResource
import kotlin.random.Random
import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.platform.LocalContext
import kotlin.text.compareTo

@Composable
fun ResponsiveLayout(windowSizeClass: WindowSizeClass){
    var selectedTab by remember { mutableStateOf(0) }

    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium) {
        // Tablet/Desktop Layout
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Sidebar(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 20.dp, top = 50.dp, bottom = 50.dp),
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )

            MainContent(
                selectedTab = selectedTab,
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
            )
        }
    } else {
        // Phone Layout
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedTab,
                    onItemSelected = { selectedTab = it }
                )
            }
        ) { padding ->
            MainContent(
                selectedTab = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFD9D9D9))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Menu icon (top-left)
            IconButton(
                onClick = { onItemSelected(0) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = menuIcon.iconResId),
                    contentDescription = menuIcon.label,
                    modifier = Modifier.size(35.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Middle group of icons
            Column(
                verticalArrangement = Arrangement.spacedBy(30.dp),
                modifier = Modifier.padding(start = 10.dp)
            ) {
                groupedIcons.forEachIndexed { index, item ->
                    IconButton(onClick = { onItemSelected(index + 1) }) {
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.label,
                            modifier = Modifier.size(35.dp),
                            tint = if (selectedIndex == index + 1) Color.Blue else Color.Black
                        )
                    }
                }
            }
        }

        // Settings icon (bottom-left)
        IconButton(
            onClick = { onItemSelected(navItems.lastIndex) },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = settingsIcon.iconResId),
                contentDescription = settingsIcon.label,
                modifier = Modifier.size(35.dp),
                tint = if (selectedIndex == navItems.lastIndex) Color.Blue else Color.Black
            )
        }
    }
}

@Composable
fun BottomNavBar(onItemSelected: (Int) -> Unit, selectedIndex: Int) {
    NavigationBar {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) }
            )

        }
    }
}

@Composable
fun MainContent(selectedTab: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Current Tab: ${navItems[selectedTab].label}")
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
