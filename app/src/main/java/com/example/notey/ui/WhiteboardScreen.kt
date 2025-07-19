package com.example.notey.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import com.example.notey.drawingmodel.DrawingTool
import com.example.notey.R
import com.example.notey.gl.GLDrawingView
import com.example.notey.ui.theme.NoteyTheme
import androidx.compose.runtime.DisposableEffect // NEW IMPORT for lifecycle management
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLifecycleOwner // NEW IMPORT for lifecycle management
import androidx.lifecycle.Lifecycle // NEW IMPORT
import androidx.lifecycle.LifecycleEventObserver // NEW IMPORT
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.notey.data.repository.NoteRepository
import com.example.notey.utils.Stroke
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun WhiteboardScreen() {
    var selectedColor by remember { mutableStateOf(0xFF000000.toInt()) } // Default to black color
    var selectedTool by remember { mutableStateOf(DrawingTool.PEN) }
    var selectedThickness by remember { mutableStateOf(8f) }

    // Get the current LifecycleOwner
    val lifecycleOwner = LocalLifecycleOwner.current

    // Use remember to hold the GLDrawingView instance
    // Make it nullable and initialize it in the factory lambda
    var glDrawingView: GLDrawingView? by remember { mutableStateOf(null) }

    // Add repository for saving strokes
    val context = LocalContext.current
    val noteRepository = remember { NoteRepository(context) }
    val noteId = remember { UUID.randomUUID().toString() }
    var lastFinishedStroke by remember { mutableStateOf<Stroke?>(null) }

    // Save strokes when they're finalized
    LaunchedEffect(lastFinishedStroke) {
        if (lastFinishedStroke != null) {
            noteRepository.saveStroke(noteId, lastFinishedStroke!!)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                // Initialize GLDrawingView here and assign it to the remembered state variable.
                GLDrawingView(context).apply {
                    glDrawingView = this // Assign the created instance to the state variable
                    setStrokeColor(selectedColor)
                    setDrawingTool(selectedTool)
                    setStrokeThickness(selectedThickness)

                    setStrokeFinishedListener(object : GLDrawingView.StrokeFinishedListener {
                        override fun onStrokeFinished(stroke: Stroke) {
                            lastFinishedStroke = stroke
                        }
                    })
                }
            },
            update = { view ->
                // This block runs on recomposition if selectedColor, selectedTool, etc. changes
                view.setStrokeColor(selectedColor)
                view.setDrawingTool(selectedTool)
                view.setStrokeThickness(selectedThickness)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Manage GLDrawingView's onResume and onPause using DisposableEffect
        DisposableEffect(lifecycleOwner, glDrawingView) { // Depend on lifecycleOwner and glDrawingView instance
            val currentGlDrawingView = glDrawingView // Capture the current instance for the observer

            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        currentGlDrawingView?.onResume() // Call onResume when the Composable's lifecycle resumes
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        currentGlDrawingView?.onPause() // Call onPause when the Composable's lifecycle pauses
                    }
                    else -> {}
                }
            }

            // Add the observer to the lifecycle
            lifecycleOwner.lifecycle.addObserver(observer)

            // When the effect leaves the composition, remove the observer
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Toolbar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            onColorSelected = { color ->
                selectedColor = color
            },
            onToolSelected = { tool ->
                selectedTool = tool
            },
            onThicknessChanged = { thickness ->
                selectedThickness = thickness
            },
            onClearCanvas = {
                glDrawingView?.clear() // Use safe call operator (?.)
            },
            onUndo = {
                glDrawingView?.undo()
            },
            onRedo = {
                glDrawingView?.redo()
            },
            selectedColor = selectedColor,
            selectedTool = selectedTool,
            selectedThickness = selectedThickness
        )
    }
}

/**
 * Composable function for the drawing application toolbar.
 * Replicates the layout and styling from the provided UI sample.
 *
 * @param modifier Modifier for this composable, allowing external customization.
 * @param selectedColor The currently selected stroke color (ARGB integer).
 * @param selectedTool The currently selected DrawingTool.
 * @param selectedThickness The currently selected stroke thickness.
 * @param onColorSelected Callback when a new color is selected (passes ARGB int).
 * @param onToolSelected Callback when a new tool is selected (passes DrawingTool enum).
 * @param onThicknessChanged Callback when a new thickness is selected (passes Float).
 * @param onClearCanvas Callback to clear the canvas.
 * @param onUndo Callback for the undo action.
 * @param onRedo Callback for the redo action.
 */
@RequiresApi(Build.VERSION_CODES.S) // Indicates that this Composable uses APIs introduced in Android S (API 31)
@Composable
fun Toolbar(
    modifier: Modifier = Modifier, // Modifier for applying adjustments to the whole toolbar
    selectedColor: Int,
    selectedTool: DrawingTool,
    selectedThickness: Float,
    onColorSelected: (Int) -> Unit,
    onToolSelected: (DrawingTool) -> Unit,
    onThicknessChanged: (Float) -> Unit,
    onClearCanvas: () -> Unit, // This maps to the swipe_down icon in your assets
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    // Define the list of available colors using ARGB hex codes
    val colors = listOf(
        0xFFE00000.toInt(), // Red
        0xFF05C41B.toInt(), // Green
        0xFF5087D9.toInt(), // Blue
        0xFF8842EB.toInt(), // Purple
        0xFF000000.toInt(), // Black
        0xFFFFFFFF.toInt()  // White
    )

    // Define the list of available thicknesses
    // These correspond to the visual representations of pen_size_1, pen_size_2, etc.
    val thicknesses = listOf(8f, 16f, 24f) // Added 24f for pen_size_5 representation

    Row(
        modifier = modifier
            .fillMaxWidth() // Makes the toolbar span the full width of its parent.
            .padding(horizontal = 16.dp, vertical = 26.dp) // Adds horizontal and vertical padding around the entire toolbar.
            .height(56.dp), // Sets a fixed height for the toolbar, matching the sample UI.
        horizontalArrangement = Arrangement.Start, // Aligns content to the start (left).
        verticalAlignment = Alignment.CenterVertically // Vertically centers content within the row.
    ) {
        // Left Pill Container: For Back and Menu buttons
        Box(
            modifier = Modifier
                .width(154.dp) // Fixed width for the left pill, as per sample UI.
                .fillMaxHeight() // Makes the pill fill the height of the parent Row.
                // Removed .shadow() to remove the drop shadow effect.
                .clip(RoundedCornerShape(percent = 50)) // Clips the outer shape of the pill.
        ) {
            // Inner Box for the frosted background effect
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fills the entire parent Box (the pill shape).
                    .background(Color.White.copy(alpha = 0.8f)) // Changed background to a more opaque white for clear visibility, adjust alpha as needed.
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(percent = 50)) // Semi-transparent white border.
                    // Apply RenderEffect.blur here for the frosted glass effect.
                    .graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            10f, // Blur radius in X direction (adjust for desired blur intensity)
                            10f, // Blur radius in Y direction (adjust for desired blur intensity)
                            Shader.TileMode.DECAL // Defines how blur handles edges (DECAL prevents repeating blurred pixels)
                        ).asComposeRenderEffect() // Converts Android RenderEffect to Compose's equivalent.
                    }
            )
            // Row for the icons, placed on top of the blurred background, remains sharp
            Row(
                modifier = Modifier
                    .fillMaxSize() // Ensures the icon row fills the entire pill area.
                    .padding(horizontal = 8.dp), // Padding inside the left pill to space icons from edges.
                horizontalArrangement = Arrangement.SpaceEvenly, // Distributes space evenly between children.
                verticalAlignment = Alignment.CenterVertically // Vertically centers icons within the row.
            ) {
                // Back Button (using arrow_back.xml)
                IconButton(onClick = { /* TODO: Implement back action, potentially from NavController if passed */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back), // Loads custom icon from drawables.
                        contentDescription = "Back", // Accessibility description.
                        tint = Color(0xff2d2d2d), // Sets icon color as per sample UI.
                        modifier = Modifier.size(32.dp) // Sets icon size.
                    )
                }
                // Menu Button (using menu.xml)
                IconButton(onClick = { /* TODO: Implement menu action */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.menu), // Loads custom icon from drawables.
                        contentDescription = "Menu", // Accessibility description.
                        tint = Color(0xff2d2d2d), // Sets icon color.
                        modifier = Modifier.size(32.dp) // Sets icon size.
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp)) // Adds horizontal space between the left and right pill containers.

        // Right Pill Container: For Undo, Redo, Clear, Tools, Colors, and Thickness
        Box(
            modifier = Modifier
                .weight(1f) // Allows this Box to take up all remaining available horizontal space.
                .fillMaxHeight() // Makes the pill fill the height of the parent Row.
                // Removed .shadow() to remove the drop shadow effect.
                .clip(RoundedCornerShape(percent = 50)) // Clips the outer shape of the pill.
        ) {
            // Inner Box for the frosted background effect
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fills the entire parent Box (the pill shape).
                    .background(Color.White.copy(alpha = 0.8f)) // Changed background to a more opaque white for clear visibility, adjust alpha as needed.
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(percent = 50)) // Semi-transparent white border.
                    // Apply RenderEffect.blur here for the frosted glass effect.
                    .graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            10f, // Blur radius in X direction
                            10f, // Blur radius in Y direction
                            Shader.TileMode.DECAL
                        ).asComposeRenderEffect()
                    }
            )
            // Row for the content, placed on top of the blurred background, remains sharp
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Ensures the inner row fills the width of its parent Box.
                    .padding(horizontal = 8.dp), // Padding inside the right pill to space content from edges.
                horizontalArrangement = Arrangement.SpaceBetween, // Distributes space evenly between major groups.
                verticalAlignment = Alignment.CenterVertically // Vertically centers content within the row.
            ) {
                // Group: Undo, Redo, Clear Canvas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between undo/redo/clear buttons.
                    verticalAlignment = Alignment.CenterVertically // Vertically centers items.
                ) {
                    // Undo Button (using undo.xml)
                    IconButton(onClick = onUndo) {
                        Icon(
                            painter = painterResource(id = R.drawable.undo), // Loads custom icon.
                            contentDescription = "Undo", // Accessibility description.
                            tint = Color(0xff2d2d2d), // Sets icon color.
                            modifier = Modifier.size(32.dp) // Sets icon size.
                        )
                    }
                    // Redo Button (using redo.xml)
                    IconButton(onClick = onRedo) {
                        Icon(
                            painter = painterResource(id = R.drawable.redo), // Loads custom icon.
                            contentDescription = "Redo", // Accessibility description.
                            tint = Color(0xff2d2d2d), // Sets icon color.
                            modifier = Modifier.size(32.dp) // Sets icon size.
                        )
                    }
                    // Clear Canvas Button (using swipe_down.xml as per your provided asset)
                    IconButton(onClick = onClearCanvas) {
                        Icon(
                            painter = painterResource(id = R.drawable.swipe_down), // Loads custom icon.
                            contentDescription = "Clear Canvas", // Accessibility description.
                            tint = Color(0xff2d2d2d), // Sets icon color.
                            modifier = Modifier.size(32.dp) // Sets icon size.
                        )
                    }
                }

                // Divider: Separates undo/redo/clear from tool selection
                Divider(
                    color = Color(0xffd0d0d0), // Color of the divider line.
                    modifier = Modifier
                        .width(1.dp) // Sets the width of the divider.
                        .height(29.dp) // Sets the height of the divider.
                        .clip(RoundedCornerShape(179.dp)) // Clips to a rounded shape (looks like a line).
                        .padding(horizontal = 4.dp) // Adds padding around the divider.
                )

                // Group: Tool Selection Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between tool buttons.
                    verticalAlignment = Alignment.CenterVertically // Vertically centers items.
                ) {
                    // Pen Tool (using stylus.xml)
                    ToolButton(
                        iconPainter = painterResource(id = R.drawable.stylus),
                        contentDescription = "Pen",
                        tool = DrawingTool.PEN,
                        selectedTool = selectedTool,
                        onToolSelected = onToolSelected
                    )
                    // Highlighter Tool (using highlighter.xml)
                    ToolButton(
                        iconPainter = painterResource(id = R.drawable.highlighter),
                        contentDescription = "Highlighter",
                        tool = DrawingTool.HIGHLIGHTER,
                        selectedTool = selectedTool,
                        onToolSelected = onToolSelected
                    )
                    // Eraser Tool (using eraser.xml)
                    ToolButton(
                        iconPainter = painterResource(id = R.drawable.eraser),
                        contentDescription = "Eraser",
                        tool = DrawingTool.ERASER,
                        selectedTool = selectedTool,
                        onToolSelected = onToolSelected
                    )
                    // Selection Tool (using lasso.xml)
                    ToolButton(
                        iconPainter = painterResource(id = R.drawable.lasso),
                        contentDescription = "Select",
                        tool = DrawingTool.SELECTION,
                        selectedTool = selectedTool,
                        onToolSelected = onToolSelected
                    )
                    // Shape Tool (using laser_pointer.xml - adjust if you have a more fitting icon)
                    ToolButton(
                        iconPainter = painterResource(id = R.drawable.laser_pointer),
                        contentDescription = "Shape",
                        tool = DrawingTool.SHAPE,
                        selectedTool = selectedTool,
                        onToolSelected = onToolSelected
                    )
                }

                // Divider: Separates tools from colors
                Divider(
                    color = Color(0xffd0d0d0), // Color of the divider line.
                    modifier = Modifier
                        .width(1.dp) // Sets the width of the divider.
                        .height(29.dp) // Sets the height of the divider.
                        .clip(RoundedCornerShape(179.dp)) // Clips to a rounded shape.
                        .padding(horizontal = 4.dp) // Adds padding around the divider.
                )

                // Group: Color Palette
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp), // Spacing between color circles.
                    contentPadding = PaddingValues(horizontal = 4.dp), // Padding around the content of the LazyRow.
                    verticalAlignment = Alignment.CenterVertically // Vertically centers color circles.
                ) {
                    items(colors) { color ->
                        ColorOption(
                            color = Color(color), // The color to display.
                            isSelected = color == selectedColor, // Determines if this color is currently selected.
                            onClick = { onColorSelected(color) } // Callback when color is clicked.
                        )
                    }
                }

                // Divider: Separates colors from thickness
                Divider(
                    color = Color(0xffd0d0d0), // Color of the divider line.
                    modifier = Modifier
                        .width(1.dp) // Sets the width of the divider.
                        .height(29.dp) // Sets the height of the divider.
                        .clip(RoundedCornerShape(179.dp)) // Clips to a rounded shape.
                        .padding(horizontal = 4.dp) // Adds padding around the divider.
                )

                // Group: Thickness Options
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between thickness options.
                    contentPadding = PaddingValues(horizontal = 4.dp), // Padding around the content of the LazyRow.
                    verticalAlignment = Alignment.CenterVertically // Vertically centers thickness options.
                ) {
                    items(thicknesses) { thickness ->
                        ThicknessOption(
                            thickness = thickness, // The thickness value to represent.
                            isSelected = thickness == selectedThickness, // Determines if this thickness is selected.
                            onClick = { onThicknessChanged(thickness) } // Callback when thickness is clicked.
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper Composable for tool selection buttons.
 */
@Composable
private fun ToolButton(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String,
    tool: DrawingTool,
    selectedTool: DrawingTool,
    onToolSelected: (DrawingTool) -> Unit
) {
    val isSelected = tool == selectedTool
    // Background color when the tool is selected
    // Changed to use Color.White with a higher alpha for a more solid white background
    val backgroundColor = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent
    // Tint color for the icon
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xff2d2d2d) // Icon color from sample UI
    // Border color when the tool is selected
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .size(40.dp) // Sets the overall size of the button container (40dp x 40dp).
            .clip(RoundedCornerShape(8.dp)) // Clips the background and border to rounded corners.
            .background(backgroundColor) // Applies the background color based on selection state.
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)) // Applies a 1dp border, colored based on selection.
            .clickable { onToolSelected(tool) }, // Makes the entire Box clickable.
        contentAlignment = Alignment.Center // Centers the icon within the Box.
    ) {
        Icon(
            painter = iconPainter, // The actual icon loaded via painterResource.
            contentDescription = contentDescription, // Accessibility description for screen readers.
            tint = contentColor, // Sets the color tint of the icon.
            modifier = Modifier.size(24.dp) // Sets the size of the icon itself.
        )
    }
}

/**
 * Helper Composable for color selection circles.
 */
@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp) // Sets the size of the circular color swatch.
            .clip(CircleShape) // Clips the Box to a perfect circle.
            .background(color) // Fills the circle with the specified color.
            .border(
                width = if (isSelected) 3.dp else 1.dp, // Thicker border when selected for visual emphasis.
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, // Dynamic border color based on selection.
                shape = CircleShape // Ensures the border is also circular.
            )
            .clickable(onClick = onClick) // Makes the color swatch clickable.
    )
}

/**
 * Helper Composable for stroke thickness selection.
 * Displays a slanted line with the representative thickness.
 */
@Composable
private fun ThicknessOption(
    thickness: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val boxSize = 36.dp // Defines the overall size of the thickness option button.
    // Color of the line drawn on the canvas, changes based on selection.
    val strokeColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xff2d2d2d) // Icon color from sample UI
    // Background color of the button, changes based on selection.
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    // Border color of the button, changes based on selection.
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .size(boxSize) // Sets the size of the clickable area for the thickness option.
            .clip(RoundedCornerShape(8.dp)) // Clips the button background to rounded corners.
            .background(backgroundColor) // Sets the background color based on selection.
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)) // Applies a 1dp border with rounded corners.
            .clickable(onClick = onClick), // Makes the Box clickable.
        contentAlignment = Alignment.Center // Centers the Canvas (line drawing) within the Box.
    ) {
        Canvas(modifier = Modifier.size(24.dp)) { // Creates a drawing canvas within the button, 24dp x 24dp.
            val startX = 0f // Start X coordinate for the line (left edge of canvas).
            val startY = size.height * 0.7f // Start Y coordinate for the line (70% down from top).
            val endX = size.width // End X coordinate for the line (right edge of canvas).
            val endY = size.height * 0.3f // End Y coordinate for the line (30% down from top).

            drawLine(
                color = strokeColor, // Color of the line.
                start = Offset(startX, startY), // Starting point of the line.
                end = Offset(endX, endY), // Ending point of the line.
                strokeWidth = thickness, // Sets the width of the line drawn on canvas, matching the actual thickness.
                cap = StrokeCap.Round // Makes the ends of the drawn line rounded for better visual.
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun PreviewWhiteboardScreen() {
    NoteyTheme {
        WhiteboardScreen()
    }
}