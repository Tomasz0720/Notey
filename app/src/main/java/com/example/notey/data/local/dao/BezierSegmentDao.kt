package com.example.notey.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.notey.data.local.model.BezierSegmentEntity

@Dao
interface BezierSegmentDao {
    @Insert
    suspend fun insertSegments(segments: List<BezierSegmentEntity>)

    @Query("SELECT * FROM bezier_segments WHERE strokeId = :strokeId")
    suspend fun getSegmentsForStroke(strokeId: Long): List<BezierSegmentEntity>
}
