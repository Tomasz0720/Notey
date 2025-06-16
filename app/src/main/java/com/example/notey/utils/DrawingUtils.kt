// com.example.notey.utils.DrawingUtils.kt

package com.example.notey.utils

import android.graphics.PointF
import android.opengl.Matrix
import kotlin.math.abs
import kotlin.math.sqrt

object DrawingUtils {

    // Helper for converting from screen coordinates to OpenGL ES world coordinates
    // (You'll need to define your OpenGL projection matrix and viewport correctly)
    fun screenToWorld(
        screenX: Float, screenY: Float,
        screenWidth: Int, screenHeight: Int,
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray
    ): PointF {
        // This is a simplified example. You'll need to properly unproject
        // the screen coordinates using your inverse view-projection matrix.

        // Normalize screen coordinates to OpenGL NDC (-1 to 1)
        val ndcX = (2.0f * screenX) / screenWidth - 1.0f
        val ndcY = 1.0f - (2.0f * screenY) / screenHeight // Y-axis inversion for screen vs GL

        val invertedMatrix = FloatArray(16)
        val tempMatrix = FloatArray(16)

        // Multiply projection and view matrices
        Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        // Invert the combined matrix
        Matrix.invertM(invertedMatrix, 0, tempMatrix, 0)

        // Create a 4D vector for the normalized device coordinates
        val outVec = FloatArray(4)
        val inVec = floatArrayOf(ndcX, ndcY, 0.0f, 1.0f) // Z=0, W=1 for 2D drawing

        // Multiply the inverted matrix by the NDC vector
        Matrix.multiplyMV(outVec, 0, invertedMatrix, 0, inVec, 0)

        // outVec will contain (worldX, worldY, worldZ, W)
        // Divide by W to get final world coordinates
        return PointF(outVec[0] / outVec[3], outVec[1] / outVec[3])
    }

    // Calculates the squared distance between two points. Faster than sqrt.
    fun distSq(p1: PointF, p2: PointF): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return dx * dx + dy * dy
    }

    // Calculates the distance between two points.
    fun dist(p1: PointF, p2: PointF): Float {
        return sqrt(distSq(p1, p2))
    }

    // Linearly interpolates between two points.
    fun lerp(a: PointF, b: PointF, t: Float): PointF {
        return PointF(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
    }

    // Other utility functions like clamping values, etc. can go here.
}