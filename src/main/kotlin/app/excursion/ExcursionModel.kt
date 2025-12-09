// app/excursion/ExcursionModel.kt
package app.excursion

import app.painting.convertation.Plain

data class ExcursionPoint(
    val id: String = generateId(),
    val plain: Plain,
    val fractalName: String,
    val colorSchemeName: String,
    val title: String = "",
    val description: String = "",
    val durationMs: Int = 2000,
    val pauseMs: Int = 1000
) {
    companion object {
        fun generateId(): String = "point_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    fun copyWithNewPlain(newPlain: Plain): ExcursionPoint {
        return copy(plain = Plain(
            xMin = newPlain.xMin,
            xMax = newPlain.xMax,
            yMin = newPlain.yMin,
            yMax = newPlain.yMax,
            width = newPlain.width,
            height = newPlain.height
        ))
    }
}

data class Excursion(
    val id: String = "excursion_${System.currentTimeMillis()}",
    var name: String = "Экскурсия",
    var description: String = "",
    val points: MutableList<ExcursionPoint> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
) {
    var isPlaying: Boolean = false
    var currentPointIndex: Int = 0

    val durationMs: Long
        get() = points.sumOf { it.durationMs.toLong() + it.pauseMs.toLong() }

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val pointCount: Int get() = points.size

    fun addPoint(point: ExcursionPoint) {
        points.add(point)
    }

    fun removePoint(pointId: String) {
        points.removeAll { it.id == pointId }
    }

    fun movePoint(fromIndex: Int, toIndex: Int) {
        if (fromIndex in points.indices && toIndex in points.indices) {
            val point = points.removeAt(fromIndex)
            points.add(toIndex, point)
        }
    }

    fun clearPoints() {
        points.clear()
        currentPointIndex = 0
        isPlaying = false
    }

    fun getCurrentPoint(): ExcursionPoint? {
        return points.getOrNull(currentPointIndex)
    }

    fun hasNextPoint(): Boolean = currentPointIndex < points.size - 1
    fun hasPreviousPoint(): Boolean = currentPointIndex > 0

    fun reset() {
        currentPointIndex = 0
        isPlaying = false
    }
}