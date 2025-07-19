package com.example.notey.data.serialization

import kotlinx.serialization.Serializable
import com.example.notey.drawingmodel.DrawingTool
import kotlinx.serialization.Contextual

@Serializable
data class SerializableStroke(
    val id: Long,
    val color: Int,
    val width: Float,
    @Contextual val tool: DrawingTool,
    val segments: List<@Contextual SerializableBezierSegment>
)