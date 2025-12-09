package app.tour

import app.painting.convertation.Plain

data class TourFrame(
    val plain: Plain,          // xMin/xMax/yMin/yMax + width/height
    val fractalName: String,
    val colorSchemeName: String,
    val durationMs: Long = 3000
)

data class FractalTour(
    val name: String = "Новая экскурсия",
    val frames: List<TourFrame> = emptyList(),
    val loop: Boolean = false
)