package com.example.notey.data.conversion

import com.example.notey.data.serialization.SerializableBezierSegment
import com.example.notey.data.serialization.SerializablePointF
import com.example.notey.data.serialization.SerializableStroke
import com.example.notey.drawingmodel.BezierSegment
import com.example.notey.drawingmodel.DrawingPoint
import com.example.notey.drawingmodel.DrawingTool

object StrokeConverter {

    fun toSerializable(
        segments: List<BezierSegment>,
        color: Int,
        width: Float
    ): SerializableStroke {
        val serializableSegments = segments.map { segment ->
            SerializableBezierSegment(
                start = SerializablePointF(segment.start.x, segment.start.y),
                control1 = SerializablePointF(segment.control1.x, segment.control1.y),
                control2 = SerializablePointF(segment.control2.x, segment.control2.y),
                end = SerializablePointF(segment.end.x, segment.end.y)
            )
        }

        return SerializableStroke(
            id = System.currentTimeMillis(),
            color = color,
            width = width,
            tool = DrawingTool.PEN,
            segments = serializableSegments
        )
    }


    fun fromSerializable(stroke: SerializableStroke): List<BezierSegment> {
        return stroke.segments.map { segment ->
            BezierSegment(
                start = DrawingPoint(x = segment.start.x, y = segment.start.y, time = System.currentTimeMillis()),
                control1 = DrawingPoint(x = segment.control1.x, y = segment.control1.y, time = System.currentTimeMillis()),
                control2 = DrawingPoint(x = segment.control2.x, y = segment.control2.y, time = System.currentTimeMillis()),
                end = DrawingPoint(x = segment.end.x, y = segment.end.y, time = System.currentTimeMillis())
            )
        }
    }
}
