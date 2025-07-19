package com.example.notey.drawingmodel

import com.example.notey.drawingmodel.DrawingPoint

data class BezierSegment(
    val start: DrawingPoint,
    val control1: DrawingPoint,
    val control2: DrawingPoint,
    val end: DrawingPoint
)
