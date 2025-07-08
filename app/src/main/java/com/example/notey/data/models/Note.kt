package com.example.notey.data.models

import com.example.notey.utils.Stroke
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val strokes: List<Stroke>
    // other note metadata
)
