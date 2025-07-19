// com.example.notey.gl.DrawingRenderer.kt

package com.example.notey.gl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.notey.utils.DrawingState
import com.example.notey.utils.Stroke // Import Stroke model
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Manages the OpenGL ES rendering.
 * Runs on a dedicated GL thread.
 */
class DrawingRenderer(private val strokeBufferManager: StrokeBufferManager) : GLSurfaceView.Renderer {
    private val TAG = "DrawingRenderer"
    companion object {
        // Vertex shader remains the same for basic position transformation
        private const val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec2 aPosition;
            void main() {
                gl_Position = uMVPMatrix * vec4(aPosition, 0.0, 1.0);
            }
        """

        // Fragment shader remains the same for solid color rendering
        private const val fragmentShaderCode = """
            precision highp float;
            uniform vec4 uColor;
            void main() {
                gl_FragColor = uColor;
            }
        """
    }

    // Shaders for rendering strokes
    private var programHandle: Int = 0
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var mvpMatrixHandle: Int = 0

    // Model-View-Projection matrix
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    // Viewport dimensions
    private var viewportWidth: Int = 0
    private var viewportHeight: Int = 0

    // Current drawing state received from UI thread
    // @Volatile is used to ensure visibility of writes to this field across threads.
    @Volatile private var drawingState: DrawingState = DrawingState(emptyList(), null)


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        try {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f) // Set clear color to white

            // Enable blending for transparency, essential for highlighter and smooth strokes
            GLES20.glEnable(GLES20.GL_BLEND)
            // Define how source and destination colors are combined for blending
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            // Initialize shaders with error checking
            programHandle = createProgram(vertexShaderCode, fragmentShaderCode)
            if (programHandle == 0) {
                throw RuntimeException("Error creating program")
            }

            // Get handles for shader attributes and uniforms with error checking
            positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
            colorHandle = GLES20.glGetUniformLocation(programHandle, "uColor")
            mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")

            if (positionHandle == -1 || colorHandle == -1 || mvpMatrixHandle == -1) {
                throw RuntimeException("Error getting shader handles")
            }

            strokeBufferManager.initialize() // Initialize StrokeBufferManager
            Matrix.setIdentityM(modelMatrix, 0) // Initialize model matrix to identity
        } catch (e: Exception) {
            Log.e(TAG, "Error in onSurfaceCreated", e)
            // Re-throw as RuntimeException to crash the app, making the error visible during development
            throw RuntimeException("Error in onSurfaceCreated", e)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to cover the entire surface
        GLES20.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height

        // Orthographic projection for 2D drawing (matches screen coordinates 1:1).
        // This makes 0,0 top-left and width,height bottom-right, mirroring Android's view system.
        Matrix.orthoM(projectionMatrix, 0, 0f, width.toFloat(), height.toFloat(), 0f, -1f, 1f)
        // Set up a simple view matrix (camera at (0,0,1), looking at origin, Y-up)
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 1f, // Camera is at (0,0,1)
            0f, 0f, 0f, // Looks at the origin
            0f, 1f, 0f  // Up vector is (0,1,0) (standard OpenGL Y-up)
        )
        // With these matrices, world coordinates directly map to screen pixels.
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear the color and depth buffers before drawing
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Combine projection and view matrices to get the final Model-View-Projection matrix
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Use the shader program for rendering
        GLES20.glUseProgram(programHandle)
        // Pass the combined MVP matrix to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        // Enable the vertex position attribute array
        GLES20.glEnableVertexAttribArray(positionHandle)

        // Draw all committed strokes. Their VBOs are prepared once they are finalized.
        for (stroke in drawingState.committedStrokes) {
            // Strokes are now drawn as GL_TRIANGLES for filled, rounded shapes
            strokeBufferManager.drawStroke(stroke, programHandle, positionHandle, colorHandle)
        }

        // Draw the active stroke (if any).
        // Its VBO is always prepared immediately before drawing because it's constantly changing.
        drawingState.activeStroke?.let {
            strokeBufferManager.prepareStrokeForRendering(it) // Always prepare active stroke buffer
            // Active stroke also drawn as GL_TRIANGLES
            strokeBufferManager.drawStroke(it, programHandle, positionHandle, colorHandle)
        }

        // Disable the vertex position attribute array after drawing
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    /**
     * Updates the drawing state from the UI thread.
     * This method simply updates the state; VBO preparation for committed strokes
     * happens in GLDrawingView when a stroke is finalized.
     * @param state The new DrawingState containing committed and active strokes.
     */
    fun setDrawingState(state: DrawingState) {
        drawingState = state
    }

    /**
     * Clears all strokes by resetting the drawing state.
     * The strokeBufferManager.clearBuffers() is called separately by GLDrawingView.
     */
    fun clear() {
        drawingState = DrawingState(emptyList(), null) // Clear internal state
    }

    /**
     * Returns a copy of the current projection matrix.
     * @return A float array representing the projection matrix.
     */
    fun getProjectionMatrix(): FloatArray {
        return projectionMatrix.copyOf()
    }

    /**
     * Returns a copy of the current view matrix.
     * @return A float array representing the view matrix.
     */
    fun getViewMatrix(): FloatArray {
        return viewMatrix.copyOf()
    }

    /**
     * Helper function to compile an OpenGL shader.
     * @param type The type of shader (e.g., GLES20.GL_VERTEX_SHADER, GLES20.GL_FRAGMENT_SHADER).
     * @param shaderCode The source code of the shader.
     * @return The OpenGL handle for the compiled shader, or 0 if compilation fails.
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type) // Create a new shader object
        GLES20.glShaderSource(shader, shaderCode) // Load the shader source code
        GLES20.glCompileShader(shader) // Compile the shader

        // Check for compilation errors
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $type: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader) // Delete the shader if compilation failed
            return 0
        }
        return shader // Return the shader handle
    }

    /**
     * Helper function to create and link an OpenGL shader program.
     * @param vertexShaderCode The source code for the vertex shader.
     * @param fragmentShaderCode The source code for the fragment shader.
     * @return The OpenGL handle for the linked program, or 0 if linking fails.
     */
    private fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES20.glCreateProgram() // Create a new program object
        GLES20.glAttachShader(program, vertexShader) // Attach vertex shader
        GLES20.glAttachShader(program, fragmentShader) // Attach fragment shader
        GLES20.glLinkProgram(program) // Link the program

        // Check for linking errors
        val linked = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            Log.e(TAG, "Could not link program: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program) // Delete the program if linking failed
            return 0
        }
        return program // Return the program handle
    }

    // Placeholder for camera control, if implemented later
    fun setCamera(translateX: Float, translateY: Float, scale: Float) {
        // Implement camera translation and scaling here by modifying the viewMatrix
        // For now, this method is empty as we are focusing on drawing strokes.
        // You would typically apply scale and translation to your viewMatrix.
        // Example: Matrix.translateM(viewMatrix, 0, -translateX, -translateY, 0f);
        // Matrix.scaleM(viewMatrix, 0, scale, scale, 1f);
    }
}
