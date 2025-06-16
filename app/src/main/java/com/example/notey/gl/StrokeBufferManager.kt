// com.example.notey.gl.StrokeBufferManager.kt

package com.example.notey.gl

import android.graphics.PointF
import android.opengl.GLES20
import android.util.Log
import com.example.notey.DrawingTool
import com.example.notey.utils.BezierSegment
import com.example.notey.utils.DrawingPoint
import com.example.notey.utils.Stroke
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.PI

/**
 * Manages OpenGL VBOs for drawing strokes.
 * Each stroke will have its own set of vertices.
 * This class will help in converting Bezier segments into triangles/lines for rendering.
 * All methods interacting with GLES20 must be called on the GL thread.
 */
class StrokeBufferManager {

    // Map to store VBO IDs for each stroke, keyed by stroke ID.
    private val strokeVBOs = mutableMapOf<Long, Int>()
    // Map to store the number of vertices for each stroke, keyed by stroke ID.
    private val strokeVertexCounts = mutableMapOf<Long, Int>()

    // Resolution for sampling Bezier curves: number of line segments per Bezier curve segment.
    private val BEZIER_RESOLUTION = 60

    // Resolution for drawing circles/semicircles (caps) - increased for smoother appearance
    private val CAP_RESOLUTION = 30 // Number of triangles to form a semi-circle

    // A single ByteBuffer for temporary use when updating VBOs.
    // This avoids constant re-allocation of native buffers, improving performance.
    // Allocated once with maximum expected size.
    private val tempFloatBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(MAX_BUFFER_SIZE_BYTES)
            .order(ByteOrder.nativeOrder()) // Use native byte order for compatibility with OpenGL
            .asFloatBuffer() // Get a FloatBuffer view of the ByteBuffer

    companion object {
        private const val FLOAT_BYTES = 4 // Size of a float in bytes
        // Max vertices for a single stroke. Each sampled point generates 2 vertices for the strip.
        // We now generate two triangles per segment + cap triangles.
        // A generous limit to prevent crashes while allowing long strokes.
        // This limit is adjusted based on the increased BEZIER_RESOLUTION.
        private const val MAX_VERTICES_PER_STROKE = 200000 // Adjusted for higher resolution
        // Max buffer size for 2 floats (x,y) per vertex.
        private const val MAX_BUFFER_SIZE_BYTES = MAX_VERTICES_PER_STROKE * 2 * FLOAT_BYTES
        private const val TAG = "StrokeBufferManager"
    }

    /**
     * Initializes the buffer manager. No specific OpenGL calls are needed here
     * as VBOs are generated on demand.
     */
    fun initialize() {
        // Nothing specific to initialize here. Maps are empty by default.
    }

    /**
     * Clears all existing VBOs and their associated data from OpenGL memory.
     * This should be called when the entire canvas is cleared.
     * MUST be called on the GL thread.
     */
    fun clearBuffers() {
        // Synchronized block to ensure thread safety if clearBuffers is called from UI thread,
        // although it should typically be queued to the GL thread.
        synchronized(this) {
            val vbosToDelete = strokeVBOs.values.toIntArray()
            if (vbosToDelete.isNotEmpty()) {
                // Delete VBOs from OpenGL context
                GLES20.glDeleteBuffers(vbosToDelete.size, vbosToDelete, 0)
                Log.d(TAG, "Deleted ${vbosToDelete.size} VBOs.")
            }
            // Clear internal maps
            strokeVBOs.clear()
            strokeVertexCounts.clear()
        }
    }

    /**
     * Removes a specific stroke's VBO from OpenGL memory and internal maps.
     * This is typically called during an undo operation.
     * MUST be called on the GL thread.
     * @param strokeId The unique ID of the stroke to remove.
     */
    fun removeStroke(strokeId: Long) {
        synchronized(this) {
            val vboId = strokeVBOs.remove(strokeId)
            if (vboId != null) {
                val vbosToDelete = intArrayOf(vboId)
                // Delete the specific VBO from OpenGL context
                GLES20.glDeleteBuffers(1, vbosToDelete, 0)
                Log.d(TAG, "Deleted VBO for stroke ID: $strokeId")
            }
            // Remove from vertex count map
            strokeVertexCounts.remove(strokeId)
        }
    }

    /**
     * Prepares or updates a VBO for a given stroke.
     * This method can be called repeatedly for the active stroke to update its buffer
     * as points are added, or once for a committed stroke when it is finalized.
     * MUST be called on the GL thread.
     * @param stroke The Stroke object to prepare.
     */
    fun prepareStrokeForRendering(stroke: Stroke) {
        // Generate vertices for the stroke to form a triangle strip
        val vertices = generateVerticesForStroke(stroke)
        if (vertices.isEmpty()) {
            // If no vertices, remove any existing VBO for this stroke and return
            removeStroke(stroke.id)
            return
        }

        synchronized(this) {
            val vertexCount = vertices.size / 2 // 2 floats (x,y) per vertex

            // Check if the vertex data exceeds the maximum buffer size.
            // If it does, log a warning and return to prevent buffer overflow and crashes.
            if (vertices.size * FLOAT_BYTES > MAX_BUFFER_SIZE_BYTES) {
                Log.w(TAG, "Stroke ID ${stroke.id} generated too many vertices (${vertices.size}). Max allowed: ${MAX_VERTICES_PER_STROKE * 2}. Stroke will not be fully rendered.")
                // To prevent app crash (OutOfMemoryError or similar), we return here.
                // In a production app, you might want to split the stroke into multiple VBOs
                // or visually indicate truncation.
                return
            }

            // Get existing VBO ID or generate a new one
            val vboId = strokeVBOs[stroke.id] ?: run {
                val vboIds = IntArray(1)
                GLES20.glGenBuffers(1, vboIds, 0) // Generate a new VBO ID
                strokeVBOs[stroke.id] = vboIds[0] // Store the new VBO ID
                vboIds[0]
            }

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId) // Bind the VBO
            tempFloatBuffer.clear() // Clear the temporary buffer
            tempFloatBuffer.put(vertices) // Put vertex data into the temporary buffer
            tempFloatBuffer.position(0) // Reset buffer position to read from the beginning

            // Upload data to the VBO.
            // GL_DYNAMIC_DRAW is used as active strokes are constantly updated.
            // GL_STATIC_DRAW could be more efficient for committed strokes if they never change,
            // but for simplicity, we use DYNAMIC here for all.
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                vertices.size * FLOAT_BYTES, // Size in bytes
                tempFloatBuffer,
                GLES20.GL_DYNAMIC_DRAW
            )
            strokeVertexCounts[stroke.id] = vertexCount // Store vertex count
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0) // Unbind the VBO
            Log.d(TAG, "Prepared stroke ID: ${stroke.id} with ${vertexCount} vertices.")
        }
    }


    /**
     * Draws a specific stroke using its prepared VBO.
     * This method assumes `prepareStrokeForRendering` has already been called for this stroke.
     * MUST be called on the GL thread.
     * @param stroke The Stroke object to draw.
     * @param shaderProgram The OpenGL shader program handle.
     * @param positionHandle The attribute location for vertex positions.
     * @param colorHandle The uniform location for stroke color.
     */
    fun drawStroke(stroke: Stroke, shaderProgram: Int, positionHandle: Int, colorHandle: Int) {
        val vboId = strokeVBOs[stroke.id]
        val vertexCount = strokeVertexCounts[stroke.id]

        // Only draw if VBO and vertex count are valid
        if (vboId == null || vertexCount == null || vertexCount == 0) {
            Log.w(TAG, "Attempted to draw stroke ID: ${stroke.id} with no valid VBO or vertex count.")
            return // Skip invalid strokes
        }

        try {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId) // Bind the stroke's VBO
            // Set the vertex attribute pointer to read from the VBO
            GLES20.glVertexAttribPointer(
                positionHandle, // Attribute location
                2,              // 2 components per vertex (x, y)
                GLES20.GL_FLOAT, // Data type is float
                false,          // Not normalized
                0,              // Stride (0 means tightly packed)
                0               // Offset (start from beginning of buffer)
            )

            // Convert ARGB color components to float (0.0 - 1.0) for OpenGL uniform
            val red = ((stroke.color shr 16) and 0xFF) / 255f
            val green = ((stroke.color shr 8) and 0xFF) / 255f
            val blue = (stroke.color and 0xFF) / 255f
            val alpha = ((stroke.color shr 24) and 0xFF) / 255f

            // Adjust alpha for highlighter tool
            val finalAlpha = if (stroke.tool == DrawingTool.HIGHLIGHTER) alpha * 0.5f else alpha // Example transparency

            // Set the uniform color for the shader
            GLES20.glUniform4f(colorHandle, red, green, blue, finalAlpha)

            // Draw the stroke as GL_TRIANGLES to achieve a filled, rounded appearance.
            // All geometry (body and caps) is now part of the same triangle set.
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0) // Unbind the VBO
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing stroke ID: ${stroke.id}", e)
            // Consider removing the problematic stroke's VBO here if drawing fails consistently,
            // to prevent repeated errors.
        }
    }

    /**
     * Generates a FloatArray of vertices for a stroke, creating a solid shape with rounded caps.
     * This involves:
     * 1. Generating vertices for the main stroke body as a series of quads (two triangles each).
     * 2. Generating vertices for circular caps at the start and end of the stroke.
     * All vertices are combined into a single array for drawing with GL_TRIANGLES.
     *
     * @param stroke The Stroke object containing Bezier segments and width.
     * @return A FloatArray where every two elements represent an (x, y) coordinate,
     * ordered for GL_TRIANGLES.
     */
    private fun generateVerticesForStroke(stroke: Stroke): FloatArray {
        val vertices = mutableListOf<Float>()
        val halfWidth = stroke.width / 2.0f

        if (stroke.segments.isEmpty()) {
            // If there's only one point (a tap), draw a small circle for the dot.
            if (stroke.pressurePoints.size == 1) {
                val point = stroke.pressurePoints.first()
                addCircleVertices(vertices, point, halfWidth, CAP_RESOLUTION)
            }
            return vertices.toFloatArray()
        }

        // --- Generate Stroke Body Vertices (Triangles from a "ribbon") ---
        // Store the previous pair of offset points to form quads
        var prevPointL: PointF? = null // Left offset point of previous sampled point
        var prevPointR: PointF? = null // Right offset point of previous sampled point

        // Iterate through each segment and sample points
        for (segmentIndex in stroke.segments.indices) {
            val segment = stroke.segments[segmentIndex]

            // Iterate through sampled points along each Bezier segment
            // We sample BEZIER_RESOLUTION + 1 points for each segment (from t=0 to t=1)
            for (i in 0..BEZIER_RESOLUTION) {
                val t = i.toFloat() / BEZIER_RESOLUTION
                val currentPoint = calculateBezierPoint(segment, t)

                // Calculate tangent for the current point.
                // We use the next point on the sampled curve, or the end of the segment/start of next.
                val tangentPoint: PointF = when {
                    // If not at the end of the current segment, use the next sampled point
                    i < BEZIER_RESOLUTION -> calculateBezierPoint(segment, (i + 1).toFloat() / BEZIER_RESOLUTION)
                    // If at the end of current segment but not the last segment overall, use start of next segment
                    segmentIndex < stroke.segments.size - 1 -> stroke.segments[segmentIndex + 1].start
                    // If at the very end of the last segment, use the current point itself (degenerate tangent)
                    else -> currentPoint
                }

                var dx = tangentPoint.x - currentPoint.x
                var dy = tangentPoint.y - currentPoint.y

                // If tangent is zero (e.g., single point or very short segment), use previous tangent if available,
                // or a default if it's the very first point.
                if (dx == 0f && dy == 0f) {
                    if (prevPointL != null && prevPointR != null) {
                        // Approximate direction from previous points
                        dx = currentPoint.x - prevPointL.x
                        dy = currentPoint.y - prevPointL.y
                    } else {
                        // If it's the very first point and no movement, assign arbitrary direction
                        dx = 1f // Arbitrary small vector to define width direction
                        dy = 0f
                    }
                }

                // Normalize tangent vector
                val length = sqrt(dx * dx + dy * dy)
                val normTanX = if (length > 0) dx / length else 0f
                val normTanY = if (length > 0) dy / length else 0f

                // Calculate perpendicular vector (rotated 90 degrees clockwise for one side, counter-clockwise for other)
                // (-y, x) for left offset, (y, -x) for right offset relative to path direction
                val perpX = -normTanY
                val perpY = normTanX

                // Calculate the two offset points (left and right edges of the stroke)
                val currentPointL = PointF(currentPoint.x + perpX * halfWidth, currentPoint.y + perpY * halfWidth)
                val currentPointR = PointF(currentPoint.x - perpX * halfWidth, currentPoint.y - perpY * halfWidth)

                if (prevPointL != null && prevPointR != null) {
                    // Add two triangles to form a quad between the previous and current points:
                    // Triangle 1: (prevPointL, prevPointR, currentPointL)
                    vertices.add(prevPointL.x); vertices.add(prevPointL.y)
                    vertices.add(prevPointR.x); vertices.add(prevPointR.y)
                    vertices.add(currentPointL.x); vertices.add(currentPointL.y)

                    // Triangle 2: (prevPointR, currentPointR, currentPointL)
                    vertices.add(prevPointR.x); vertices.add(prevPointR.y)
                    vertices.add(currentPointR.x); vertices.add(currentPointR.y)
                    vertices.add(currentPointL.x); vertices.add(currentPointL.y)
                }

                prevPointL = currentPointL
                prevPointR = currentPointR
            }
        }

        // --- Generate Rounded Caps ---
        // Only add caps if there are enough points to define a clear direction
        if (stroke.segments.isNotEmpty()) {
            // Start Cap
            val firstPoint = stroke.segments.first().start
            val firstSampledPoint = calculateBezierPoint(stroke.segments.first(), 0.01f) // Point slightly along the first segment
            // The direction *from* the first point *to* the sampled point defines the stroke's initial direction.
            // The cap should face the opposite way (i.e., this vector points from the cap's center into the stroke body).
            val capDirectionX_start = firstPoint.x - firstSampledPoint.x
            val capDirectionY_start = firstPoint.y - firstSampledPoint.y
            addCapVertices(vertices, firstPoint, halfWidth, capDirectionX_start, capDirectionY_start)

            // End Cap
            val lastPoint = stroke.segments.last().end
            val lastSampledPoint = calculateBezierPoint(stroke.segments.last(), 0.99f) // Point slightly before the last point
            // The direction *from* the sampled point *to* the last point defines the stroke's final direction.
            // The cap should face this way (i.e., this vector points from the cap's center into the stroke body).
            val capDirectionX_end = lastPoint.x - lastSampledPoint.x
            val capDirectionY_end = lastPoint.y - lastSampledPoint.y
            addCapVertices(vertices, lastPoint, halfWidth, capDirectionX_end, capDirectionY_end)
        }


        return vertices.toFloatArray()
    }

    /**
     * Adds vertices for a circular cap (semicircle formed by triangles) to the main vertex list.
     * The cap is centered at `centerPt` and its diameter is perpendicular to `capDirectionX`, `capDirectionY`.
     * This `capDirection` vector points *from* the center of the cap *towards* the main stroke body.
     * The semicircle will sweep 180 degrees *away* from this direction.
     *
     * @param centerPt The center point of the circular cap.
     * @param radius The radius of the circular cap (half of the stroke width).
     * @param capDirectionX The X component of the vector indicating the direction
     * from the cap's center towards the stroke body.
     * @param capDirectionY The Y component of the vector indicating the direction
     * from the cap's center towards the stroke body.
     */
    private fun addCapVertices(vertices: MutableList<Float>, centerPt: PointF, radius: Float, capDirectionX: Float, capDirectionY: Float) {
        // Calculate the angle of the `capDirection` vector relative to the positive X-axis.
        val capAngleRad = atan2(capDirectionY, capDirectionX)

        // The angle interval for each step
        val angleStep = PI.toFloat() / CAP_RESOLUTION

        // The semicircle needs to sweep 180 degrees perpendicular to `capDirection`.
        // If `capDirection` is along X+, the sweep should be from Y- to Y+.
        // The sweep starts from `capAngleRad - PI/2` and goes to `capAngleRad + PI/2`.
        val startSweepAngle = capAngleRad - (PI / 2).toFloat()
        val endSweepAngle = capAngleRad + (PI / 2).toFloat()

        var prevX = centerPt.x + radius * cos(startSweepAngle)
        var prevY = centerPt.y + radius * sin(startSweepAngle)

        for (i in 1..CAP_RESOLUTION) {
            val angle = startSweepAngle + (endSweepAngle - startSweepAngle) * i / CAP_RESOLUTION.toFloat()
            val currentX = centerPt.x + radius * cos(angle)
            val currentY = centerPt.y + radius * sin(angle)

            // Add triangle: (center, prevPoint, currentPoint)
            vertices.add(centerPt.x); vertices.add(centerPt.y)
            vertices.add(prevX); vertices.add(prevY)
            vertices.add(currentX); vertices.add(currentY)

            prevX = currentX
            prevY = currentY
        }
    }


    /**
     * Calculates a point on a cubic Bezier curve given its four control points and parameter t.
     * B(t) = (1-t)^3 * P0 + 3 * (1-t)^2 * t * P1 + 3 * (1-t) * t^2 * P2 + t^3 * P3
     * @param segment The BezierSegment containing start, control1, control2, and end points.
     * @param t The parameter, typically between 0.0 and 1.0.
     * @return The PointF representing the calculated point on the curve.
     */
    private fun calculateBezierPoint(segment: BezierSegment, t: Float): PointF {
        val tSq = t * t
        val tCub = tSq * t
        val oneMinusT = 1 - t
        val oneMinusTSq = oneMinusT * oneMinusT
        val oneMinusTCub = oneMinusTSq * oneMinusT

        // Calculate X coordinate
        val x = oneMinusTCub * segment.start.x +
                3 * oneMinusTSq * t * segment.control1.x +
                3 * oneMinusT * tSq * segment.control2.x +
                tCub * segment.end.x

        // Calculate Y coordinate
        val y = oneMinusTCub * segment.start.y +
                3 * oneMinusTSq * t * segment.control1.y +
                3 * oneMinusT * tSq * segment.control2.y +
                tCub * segment.end.y

        return PointF(x, y)
    }

    /**
     * Helper to add circle vertices for a dot (e.g., for single tap).
     * This draws a full circle as a triangle fan.
     *
     * @param vertices The mutable list of floats to add vertex data to.
     * @param center The center point of the circle.
     * @param radius The radius of the circle.
     * @param resolution The number of triangles to use for the circle, higher for smoother.
     */
    private fun addCircleVertices(vertices: MutableList<Float>, center: DrawingPoint, radius: Float, resolution: Int) {
        val angleIncrement = (2 * PI / resolution).toFloat()

        var prevX = center.x + radius * cos(0f)
        var prevY = center.y + radius * sin(0f)

        for (i in 1..resolution) {
            val angle = i * angleIncrement
            val currentX = center.x + radius * cos(angle)
            val currentY = center.y + radius * sin(angle)

            // Each triangle of the fan shares the center point
            vertices.add(center.x)
            vertices.add(center.y)
            vertices.add(prevX)
            vertices.add(prevY)
            vertices.add(currentX)
            vertices.add(currentY)

            prevX = currentX
            prevY = currentY
        }
    }
}