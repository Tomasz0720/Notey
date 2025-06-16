// com.example.notey.gl.GLDrawingView.kt

package com.example.notey.gl

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.opengl.EGL14.EGL_OPENGL_ES2_BIT
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.example.notey.DrawingTool
import com.example.notey.utils.BezierCurveFitter
import com.example.notey.utils.BezierSegment
import com.example.notey.utils.DrawingPoint
import com.example.notey.utils.DrawingState
import com.example.notey.utils.DrawingUtils
import com.example.notey.utils.Stroke
import java.util.UUID

// EGL imports for advanced configuration
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10

class GLDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var lastRenderTime = 0L
    private val FRAME_TIME_MILLIS = 16L // Approximately 60 FPS

    // Defines how many raw points a stroke can have before it's "chunked"
    // and a new stroke segment begins. This prevents individual strokes from becoming too large.
    // This now refers to 'filtered' points.
    private val CHUNK_SIZE_POINTS = 70 // Adjust as needed, e.g., 50-100 filtered points

    // Minimum squared distance a new point must be from the last one to be added.
    // Helps filter noise from stylus input and ensures points are sufficiently spaced for fitting.
    private val MIN_POINT_DIST_SQ = 3f * 3f // Example: 5 pixels squared

    /**
     * Requests a render, but only if enough time has passed since the last request.
     * This helps to reduce rendering load when many touch events occur rapidly.
     */
    private fun throttledRequestRender() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRenderTime >= FRAME_TIME_MILLIS) {
            requestRender()
            lastRenderTime = currentTime
        }
    }

    private val TAG = "GLDrawingView"
    // Using a lock object for thread safety when modifying shared state accessible
    // by both UI and GL threads.
    private val renderLock = Object()

    private val strokeBufferManager = StrokeBufferManager()
    private val drawingRenderer: DrawingRenderer = DrawingRenderer(strokeBufferManager)
    private val bezierCurveFitter = BezierCurveFitter() // Instance of the fitter

    // State for the currently active stroke points (raw points from touch)
    // This list now holds *filtered* points only for the *current segment* of the stroke.
    private var currentRawPoints = mutableListOf<DrawingPoint>()

    // State for the currently active stroke (Bezier segments generated from currentRawPoints)
    // This now represents the *current chunk* being drawn.
    private var currentActiveStroke: Stroke? = null

    // List of finished strokes that are committed to the canvas.
    // This will now contain multiple 'Stroke' objects for a single continuous user drawing.
    private val finishedStrokes = mutableListOf<Stroke>()

    // Current drawing properties (from Toolbar), managed by MainActivity
    private var currentColor: Int = Color.BLACK
    private var currentTool: DrawingTool = DrawingTool.PEN
    private var currentThickness: Float = 4f

    // Camera state (for pan/zoom) - assumed existing from previous snippets
    // Not directly used in the drawing logic below but kept for completeness
    private var currentScale = 1.0f
    private var currentPanX = 0.0f
    private var currentPanY = 0.0f

    // Public method for changing drawing color (called from Toolbar)
    fun setStrokeColor(color: Int) {
        currentColor = color
    }

    // Public method for changing drawing tool (called from Toolbar)
    fun setDrawingTool(tool: DrawingTool) {
        currentTool = tool
    }

    // Public method for changing stroke thickness (called from Toolbar)
    fun setStrokeThickness(thickness: Float) {
        currentThickness = thickness
    }

    // Public method for clearing the canvas
    fun clear() {
        // All state modifications should be synchronized
        synchronized(renderLock) {
            finishedStrokes.clear()
            currentActiveStroke = null
            currentRawPoints.clear()
            bezierCurveFitter.startNewStroke() // Clear fitter's internal state
            // Queue OpenGL operations to run on the GL thread
            queueEvent {
                strokeBufferManager.clearBuffers() // Clear all VBOs in renderer
                drawingRenderer.setDrawingState(DrawingState(emptyList(), null))
            }
            requestRender() // Request a render to show the cleared canvas
        }
    }

    // Public method for undoing the last stroke
    fun undo() {
        // All state modifications should be synchronized
        synchronized(renderLock) {
            if (finishedStrokes.isNotEmpty()) {
                val lastStroke = finishedStrokes.removeLast()
                // Queue OpenGL operations to run on the GL thread
                queueEvent {
                    strokeBufferManager.removeStroke(lastStroke.id) // Remove its VBO from GL memory
                    drawingRenderer.setDrawingState(
                        DrawingState(
                            finishedStrokes.toList(), // Pass current list of committed strokes
                            currentActiveStroke // Preserve the active stroke if any
                        )
                    )
                }
                requestRender() // Request a render to reflect the undo
            }
        }
    }

    fun redo() {
        // TODO: Implement redo logic in GLDrawingView.
        // You'd need a 'redoShelf' list to temporarily store undone strokes.
        // When undo is called, move the stroke from finishedStrokes to redoShelf.
        // When redo is called, move the stroke from redoShelf back to finishedStrokes.
        // Any new drawing action should clear the redoShelf.
        Log.d(TAG, "Redo not implemented.")
    }

    init {
        Log.d(TAG, "Initializing GLDrawingView")
        try {
            // Set OpenGL ES 2.0 context
            setEGLContextClientVersion(2)

            // Configure the EGL context to request multisampling (anti-aliasing).
            // This is crucial for smoothing out the pixelated edges of strokes.
            setEGLConfigChooser(object : GLSurfaceView.EGLConfigChooser {
                override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
                    try {
                        val attribs = intArrayOf(
                            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                            EGL10.EGL_RED_SIZE, 8,
                            EGL10.EGL_GREEN_SIZE, 8,
                            EGL10.EGL_BLUE_SIZE, 8,
                            EGL10.EGL_ALPHA_SIZE, 8,
                            EGL10.EGL_DEPTH_SIZE, 16,
                            EGL10.EGL_SAMPLE_BUFFERS, 1, // Request anti-aliasing
                            EGL10.EGL_SAMPLES, 4,       // Increase from 2 to 4 for better smoothing
                            EGL10.EGL_NONE
                        )

                        val configs = arrayOfNulls<EGLConfig>(1)
                        val numConfigs = IntArray(1)

                        if (egl.eglChooseConfig(display, attribs, configs, 1, numConfigs)) {
                            if (numConfigs[0] > 0 && configs[0] != null) {
                                return configs[0]!!
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("GLDrawingView", "Failed to choose config with antialiasing", e)
                    }

                    // Fallback to no anti-aliasing
                    val attribs = intArrayOf(
                        EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_RED_SIZE, 8,
                        EGL10.EGL_GREEN_SIZE, 8,
                        EGL10.EGL_BLUE_SIZE, 8,
                        EGL10.EGL_ALPHA_SIZE, 8,
                        EGL10.EGL_DEPTH_SIZE, 16,
                        EGL10.EGL_NONE
                    )

                    val configs = arrayOfNulls<EGLConfig>(1)
                    val numConfigs = IntArray(1)

                    if (!egl.eglChooseConfig(display, attribs, configs, 1, numConfigs)) {
                        throw IllegalArgumentException("Failed to choose configuration")
                    }

                    if (numConfigs[0] <= 0) {
                        throw IllegalArgumentException("No configs match requested attributes")
                    }

                    return configs[0]!!
                }
            })

            // Set the custom renderer for OpenGL operations
            setRenderer(drawingRenderer)
            // Render only when data changes, to save battery and CPU cycles
            renderMode = RENDERMODE_WHEN_DIRTY

            // Preserve the EGL context when GLSurfaceView pauses,
            // preventing resource loss and flicker on app resume.
            preserveEGLContextOnPause = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing GLDrawingView", e)
            // Re-throw as RuntimeException to make initialization errors immediately apparent
            throw RuntimeException("Error initializing GLDrawingView", e)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Synchronize access to shared mutable state to prevent race conditions
        synchronized(renderLock) {
            val screenWidth = width
            val screenHeight = height

            // Get projection and view matrices from the renderer.
            // Copies are returned to ensure thread safety.
            val projectionMatrix = drawingRenderer.getProjectionMatrix()
            val viewMatrix = drawingRenderer.getViewMatrix()

            // Convert screen touch coordinates to OpenGL world coordinates
            val worldPoint = DrawingUtils.screenToWorld(
                event.x, event.y,
                screenWidth, screenHeight,
                projectionMatrix, viewMatrix
            )

            val newDrawingPoint = DrawingPoint(worldPoint.x, worldPoint.y, event.pressure)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(
                        TAG,
                        "ACTION_DOWN: ${event.x}, ${event.y} -> World: ${worldPoint.x}, ${worldPoint.y}"
                    )
                    bezierCurveFitter.startNewStroke() // Reset fitter (if it had internal state)
                    currentRawPoints.clear() // Clear current raw points list
                    currentRawPoints.add(newDrawingPoint) // Add first point (always added)

                    // Initialize a new active stroke with a unique ID
                    // Segments will be empty initially, will be filled on MOVE
                    currentActiveStroke = Stroke(
                        id = UUID.randomUUID().mostSignificantBits,
                        segments = emptyList(), // No segments yet
                        color = currentColor,
                        width = currentThickness,
                        tool = currentTool, // Include the current tool
                        pressurePoints = mutableListOf(newDrawingPoint) // Start with the first point
                    )

                    // Update the renderer's drawing state with the new active stroke
                    // This is queued to run on the GL thread.
                    queueEvent {
                        drawingRenderer.setDrawingState(
                            DrawingState(
                                finishedStrokes.toList(), // The list of already committed strokes
                                currentActiveStroke // The stroke currently being drawn
                            )
                        )
                    }
                    throttledRequestRender() // Request a render to show the start of the stroke
                }

                MotionEvent.ACTION_MOVE -> {
                    // Log.d(TAG, "ACTION_MOVE: ${event.x}, ${event.y} -> World: ${worldPoint.x}, ${worldPoint.y}")

                    // Apply distance filtering before adding the point to currentRawPoints
                    if (currentRawPoints.isEmpty() || DrawingUtils.distSq(
                            PointF(newDrawingPoint.x, newDrawingPoint.y),
                            PointF(currentRawPoints.last().x, currentRawPoints.last().y)
                        ) > MIN_POINT_DIST_SQ
                    ) {
                        currentRawPoints.add(newDrawingPoint) // Add the new filtered point
                        currentActiveStroke?.pressurePoints?.add(newDrawingPoint) // Add to active stroke's raw points for segment fitting
                    }

                    // If enough FILTERED points are accumulated for the current chunk, finalize it and start a new one
                    if (currentRawPoints.size >= CHUNK_SIZE_POINTS) {
                        // 1. Finalize the current active stroke (chunk)
                        // Use a copy of currentRawPoints for fitting before clearing
                        val chunkSegments = bezierCurveFitter.fitAndGetBezierCurves(currentRawPoints.toList())
                        val committedChunk = currentActiveStroke!!.copy(segments = chunkSegments)
                        finishedStrokes.add(committedChunk)

                        // Queue VBO preparation for the finalized chunk on the GL thread
                        queueEvent {
                            strokeBufferManager.prepareStrokeForRendering(committedChunk)
                        }

                        // 2. Start a new active stroke (new chunk)
                        // Take the last few points to ensure visual continuity between chunks
                        // We need at least 2 points for a segment; 3-4 is safer for curve fitting.
                        val continuityPoints = currentRawPoints.takeLast(3).toMutableList()
                        if (continuityPoints.isEmpty()) {
                            // Fallback if somehow takeLast(3) returns empty (e.g., if CHUNK_SIZE_POINTS was 1 or 2)
                            continuityPoints.add(newDrawingPoint)
                        }

                        currentRawPoints.clear() // Clear points for the old chunk
                        currentRawPoints.addAll(continuityPoints) // Add continuity points to new chunk

                        // Re-initialize a new active stroke with a new UUID for the new chunk
                        currentActiveStroke = Stroke(
                            id = UUID.randomUUID().mostSignificantBits, // New unique ID for the new chunk
                            segments = emptyList(), // Will be populated in this same MOVE event or next
                            color = currentColor,
                            width = currentThickness,
                            tool = currentTool,
                            pressurePoints = continuityPoints // Start new stroke with continuity points
                        )
                        // Note: bezierCurveFitter doesn't need to be reset and re-added points.
                        // It always gets the 'currentRawPoints.toList()' for fitting.
                    }

                    // Generate Bezier segments for the current (potentially new) active stroke chunk
                    // This is always done, whether a chunk was finalized or not, to update the active stroke.
                    val currentSegments = bezierCurveFitter.fitAndGetBezierCurves(currentRawPoints.toList())
                    currentActiveStroke = currentActiveStroke?.copy(segments = currentSegments)

                    // Update the renderer with the latest drawing state (committed chunks + current active chunk)
                    queueEvent {
                        drawingRenderer.setDrawingState(
                            DrawingState(
                                finishedStrokes.toList(),
                                currentActiveStroke
                            )
                        )
                    }
                    throttledRequestRender() // Request render to draw the partial stroke
                }

                MotionEvent.ACTION_UP -> {
                    Log.d(
                        TAG,
                        "ACTION_UP: ${event.x}, ${event.y} -> World: ${worldPoint.x}, ${worldPoint.y}"
                    )
                    // Always add the final point if it wasn't added due to filtering or if it's a single tap.
                    // This ensures the last point is always included in the final segment.
                    if (currentRawPoints.isEmpty() || DrawingUtils.distSq(
                            PointF(newDrawingPoint.x, newDrawingPoint.y),
                            PointF(currentRawPoints.last().x, currentRawPoints.last().y)
                        ) > MIN_POINT_DIST_SQ || currentRawPoints.size < 2 // Ensure at least 2 points for fitting
                    ) {
                        currentRawPoints.add(newDrawingPoint)
                        currentActiveStroke?.pressurePoints?.add(newDrawingPoint)
                    }


                    // Finalize Bezier segments for the active stroke (the very last chunk) before committing
                    val finalSegments = if (currentRawPoints.size >= 2) {
                        bezierCurveFitter.fitAndGetBezierCurves(currentRawPoints.toList())
                    } else {
                        // Handle single-point tap: create a tiny stroke (a dot)
                        listOf(
                            BezierSegment(
                                start = worldPoint,
                                control1 = worldPoint,
                                control2 = worldPoint,
                                end = worldPoint
                            )
                        )
                    }

                    if (currentActiveStroke != null) {
                        val finalStroke = currentActiveStroke!!.copy(segments = finalSegments)
                        finishedStrokes.add(finalStroke) // Add to the list of finished strokes
                        // Prepare the VBO for the newly committed final chunk ONCE here.
                        queueEvent {
                            strokeBufferManager.prepareStrokeForRendering(finalStroke)
                        }
                    }

                    currentActiveStroke = null // Clear the active stroke as drawing is finished
                    currentRawPoints.clear() // Clear raw points for the next full stroke
                    bezierCurveFitter.startNewStroke() // Clear fitter's internal state for the next stroke

                    // Update the renderer to draw only committed strokes (no active stroke anymore)
                    // This is queued to run on the GL thread.
                    queueEvent {
                        drawingRenderer.setDrawingState(
                            DrawingState(
                                finishedStrokes.toList(),
                                null // No active stroke
                            )
                        )
                    }
                    requestRender() // Request a final render
                }
            }

            return true // Indicate that the event was consumed
        }
    }
}