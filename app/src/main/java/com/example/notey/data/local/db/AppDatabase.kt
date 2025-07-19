package com.example.notey.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notey.data.local.dao.BezierSegmentDao
import com.example.notey.data.local.dao.NoteDao
import com.example.notey.data.local.dao.StrokeDao
import com.example.notey.data.local.model.NoteEntity
import com.example.notey.data.local.model.StrokeEntity
import com.example.notey.data.local.model.BezierSegmentEntity

//@Database(
//    entities = [NoteEntity::class, StrokeEntity::class, BezierSegmentEntity::class],
//    version = 1
//)

//@TypeConverters(Converters::class)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun noteDao(): NoteDao
//    abstract fun strokeDao(): StrokeDao
//    abstract fun bezierSegmentDao(): BezierSegmentDao
//}

//val db = Room.databaseBuilder(
//    context,
//    AppDatabase::class.java,
//    "notes_database"
//).build()
