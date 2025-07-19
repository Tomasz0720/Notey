package com.example.notey.data.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializablePointF(
    val x: Float,
    val y: Float
)