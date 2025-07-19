package com.example.notey.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notey.data.local.model.StrokeEntity

@Dao
interface StrokeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStroke(stroke: StrokeEntity)

    @Query("SELECT * FROM strokes WHERE noteId = :noteId")
    suspend fun getStrokesForNote(noteId: Long): List<StrokeEntity>

    @Delete
    suspend fun deleteStroke(stroke: StrokeEntity)
}
