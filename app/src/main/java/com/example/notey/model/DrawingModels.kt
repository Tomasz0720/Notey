// com.example.notey.utils.DrawingModels.kt

package com.example.notey.utils

import android.graphics.PointF
import com.example.notey.model.DrawingTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SerializablePointF(
    val x: Float,
    val y: Float
)

/**
 * Represents a single point captured during a drawing stroke.
 * Includes pressure if available from the stylus.
 */

@Serializable
data class DrawingPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f // Default pressure to 1.0 if not available
)

/**
 * Represents a single Bezier curve segment.
 * This is crucial for resolution-independent drawing.
 */
@Serializable
data class BezierSegment(
    val start: SerializablePointF,      // P0
    val control1: SerializablePointF,   // P1
    val control2: SerializablePointF,   // P2
    val end: SerializablePointF         // P3
)

/**
 * Represents a complete drawing stroke, composed of Bezier segments.
 */
@Serializable
data class Stroke(
    val id: Long, // Unique ID for undo/redo, saving, etc.
    val segments: List<BezierSegment>,
    val color: Int, // ARGB color
    val width: Float, // Stroke width in pixels (will be scaled by zoom)
    val tool: DrawingTool, // Add drawing tool to differentiate rendering (e.g., for highlighter)
    val pressurePoints: MutableList<DrawingPoint> // Store raw points with pressure for advanced rendering (optional, but good for refitting)
)

/**
 * Represents the current drawing state, separating committed (finished) strokes
 * from the single active (being drawn) stroke.
 */
@Serializable
data class DrawingState(
    val committedStrokes: List<Stroke>,
    val activeStroke: Stroke? = null
)