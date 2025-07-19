package com.example.notey.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.notey.drawingmodel.DrawingTool

@Entity(
    tableName = "strokes",
    foreignKeys = [ForeignKey(
        entity = NoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("noteId")]
)
data class StrokeEntity(
    @PrimaryKey val id: Long,
    val noteId: Long,
    val color: Int,
    val width: Float,
    val tool: DrawingTool
)
