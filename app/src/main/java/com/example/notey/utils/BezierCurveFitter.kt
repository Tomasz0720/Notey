// com.example.notey.utils.BezierCurveFitter.kt

package com.example.notey.utils

import android.graphics.PointF
import kotlin.math.sqrt

/**
 * Manages the process of fitting Bezier curves to a series of raw input points.
 * This class no longer maintains an internal list of raw points or filters them.
 * It strictly takes a list of raw points and fits Bezier curves to them.
 */
class BezierCurveFitter {

    // Call this when a stroke starts (ACTION_DOWN) - now primarily for clearing internal state if any
    fun startNewStroke() {
        // No internal raw points list to clear here anymore.
        // This method can be kept if the fitter ever gains other internal state to reset.
    }

    /**
     * Generates and returns Bezier segments from the provided raw points.
     * This method expects a list of points that are already filtered/processed as needed.
     * @param rawPoints A list of DrawingPoint objects representing the stroke points.
     */
    fun fitAndGetBezierCurves(rawPoints: List<DrawingPoint>): List<BezierSegment> {
        val segments = mutableListOf<BezierSegment>()

        if (rawPoints.size < 2) {
            return emptyList()
        }

        // Handle the simplest case: a single line segment for 2 points
        if (rawPoints.size == 2) {
            val p0 = SerializablePointF(rawPoints[0].x, rawPoints[0].y)
            val p1 = SerializablePointF(rawPoints[1].x, rawPoints[1].y)
            segments.add(
                BezierSegment(
                    start = p0,
                    control1 = p0, // Straight line - control points are start/end
                    control2 = p1, // Straight line
                    end = p1
                )
            )
            return segments
        }

        // For 3 or more points, apply cubic Bezier fitting (simplified)
        // This attempts to create smoother curves by calculating control points
        // based on the previous and next points.
        for (i in 0 until rawPoints.size - 1) {
            val p0x = rawPoints[i].x
            val p0y = rawPoints[i].y
            val p3x = rawPoints[i + 1].x
            val p3y = rawPoints[i + 1].y

            // Get previous and next points to approximate tangents
            // Use p0/p3 themselves if at the very start/end of the rawPoints list
            val prevPx = if (i > 0) rawPoints[i-1].x else p0x
            val prevPy = if (i > 0) rawPoints[i-1].y else p0y
            val nextPx = if (i + 2 < rawPoints.size) rawPoints[i+2].x else p3x
            val nextPy = if (i + 2 < rawPoints.size) rawPoints[i+2].y else p3y

            // Calculate tangent direction for p0 (based on vector from prevP to p3)
            val tan0x = p3x - prevPx
            val tan0y = p3y - prevPy
            val len0 = sqrt(tan0x*tan0x + tan0y*tan0y)
            val normTan0x = if (len0 > 0) tan0x / len0 else 0f
            val normTan0y = if (len0 > 0) tan0y / len0 else 0f

            // Calculate tangent direction for p3 (based on vector from p0 to nextP)
            val tan1x = nextPx - p0x
            val tan1y = nextPy - p0y
            val len1 = sqrt(tan1x*tan1x + tan1y*tan1y)
            val normTan1x = if (len1 > 0) tan1x / len1 else 0f
            val normTan1y = if (len1 > 0) tan1y / len1 else 0f

            // Control point distances (tune this for desired curve smoothness)
            // A higher tension makes the curve "tighter" to the original path.
            val tension = 0.3f // Adjust for more or less "curvy" lines
            val dist = sqrt((p3x-p0x)*(p3x-p0x) + (p3y-p0y)*(p3y-p0y)) * tension

            // Calculate control points c1 and c2 as SerializablePointF
            val c1 = SerializablePointF(p0x + normTan0x * dist, p0y + normTan0y * dist)
            val c2 = SerializablePointF(p3x - normTan1x * dist, p3y - normTan1y * dist)

            // Add the new Bezier segment
            segments.add(
                BezierSegment(
                    start = SerializablePointF(p0x, p0y),
                    control1 = c1,
                    control2 = c2,
                    end = SerializablePointF(p3x, p3y)
                )
            )
        }

        return segments
    }
}