package com.example.notey.drawingmodel

import kotlinx.serialization.Serializable

@Serializable
enum class DrawingTool {
    PEN,
    HIGHLIGHTER,
    ERASER,
    SELECTION, // Added for the selection tool (dashed rectangle)
    SHAPE      // Added for the laser tool (zigzag line)
}