package com.example.notey.drawingmodel

data class DrawingPoint(
    val x: Float,
    val y: Float,
    val time: Long // useful for velocity/thinning brushes later
)