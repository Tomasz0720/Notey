package com.example.notey.data.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializableBezierSegment(
    val start: SerializablePointF,
    val control1: SerializablePointF,
    val control2: SerializablePointF,
    val end: SerializablePointF
)
