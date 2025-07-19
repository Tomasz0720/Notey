package com.example.notey.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bezier_segments",
    foreignKeys = [ForeignKey(
        entity = StrokeEntity::class,
        parentColumns = ["id"],
        childColumns = ["strokeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("strokeId")]
)
data class BezierSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val strokeId: Long,
    val startX: Float,
    val startY: Float,
    val control1X: Float,
    val control1Y: Float,
    val control2X: Float,
    val control2Y: Float,
    val endX: Float,
    val endY: Float
)
