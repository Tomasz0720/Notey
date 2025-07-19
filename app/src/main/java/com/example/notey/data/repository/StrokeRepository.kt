package com.example.notey.data.repository

import android.content.Context
import com.example.notey.data.serialization.SerializableStroke
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class StrokeRepository(private val context: Context) {

    private val json = Json { prettyPrint = true }

    fun saveToFile(stroke: SerializableStroke, fileName: String) {
        val file = File(context.filesDir, "$fileName.json")
        file.writeText(json.encodeToString(stroke))
    }

    fun loadFromFile(fileName: String): SerializableStroke? {
        val file = File(context.filesDir, "$fileName.json")
        if (!file.exists()) return null
        return json.decodeFromString(file.readText())
    }
}
