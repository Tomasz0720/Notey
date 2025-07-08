package com.example.notey.data

import android.content.Context
import com.example.notey.data.models.Note
import com.example.notey.utils.Stroke
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class NoteRepository(private val context: Context) {
    private val notesDir: File = File(context.filesDir, "notes").apply { mkdirs() }

    fun getNoteFile(noteId: String): File {
        return File(notesDir, "$noteId.json")
    }

    fun saveNote(noteId: String, strokes: List<Stroke>) {
        val file = getNoteFile(noteId)
        file.writeText(Json.encodeToString(strokes))
    }

    fun loadNote(noteId: String): List<Stroke> {
        val file = getNoteFile(noteId)
        return if (file.exists()) {
            Json.decodeFromString(file.readText())
        } else {
            emptyList()
        }
    }

    fun deleteNote(noteId: String) {
        getNoteFile(noteId).delete()
    }

    fun saveStroke(noteId: String, stroke: Stroke) {
        val file = getNoteFile(noteId)
        val strokes = if (file.exists()) {
            Json.decodeFromString<List<Stroke>>(file.readText()).toMutableList()
        } else {
            mutableListOf()
        }
        strokes.add(stroke)
        file.writeText(Json.encodeToString(strokes))
    }
}