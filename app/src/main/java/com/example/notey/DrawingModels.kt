// com.example.notey.utils.DrawingModels.kt

package com.example.notey.utils

import android.graphics.Color // For ARGB values in Stroke
import android.graphics.PointF // Use PointF for float coordinates
import com.example.notey.DrawingTool // Ensure DrawingTool is imported

/**
 * Represents a single point captured during a drawing stroke.
 * Includes pressure if available from the stylus.
 */
data class DrawingPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f // Default pressure to 1.0 if not available
)

/**
 * Represents a single Bezier curve segment.
 * This is crucial for resolution-independent drawing.
 */
data class BezierSegment(
    val start: PointF,      // P0
    val control1: PointF,   // P1
    val control2: PointF,   // P2
    val end: PointF         // P3
)

/**
 * Represents a complete drawing stroke, composed of Bezier segments.
 */
data class Stroke(
    val id: Long, // Unique ID for undo/redo, saving, etc.
    val segments: List<BezierSegment>,
    val color: Int, // ARGB color
    val width: Float, // Stroke width in pixels (will be scaled by zoom)
    val tool: DrawingTool, // Add drawing tool to differentiate rendering (e.g., for highlighter)
    val pressurePoints: MutableList<DrawingPoint> // Store raw points with pressure for advanced rendering (optional, but good for refitting)
) {
    // Add a helper function to approximate length or bounding box if needed
}

/**
 * Represents the current drawing state, separating committed (finished) strokes
 * from the single active (being drawn) stroke.
 */
data class DrawingState(
    val committedStrokes: List<Stroke>,
    val activeStroke: Stroke?
)