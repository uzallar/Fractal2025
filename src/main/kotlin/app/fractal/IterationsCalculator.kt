package app.fractal

import app.painting.convertation.Plain
import kotlin.math.ln
import kotlin.math.max

object IterationsCalculator {

    private const val INITIAL_WIDTH = 3.0 // от -2.0 до +1.0 ширина т.к фрактал вмещается в круг с рад=2

    fun getMaxIterations(plain: Plain): Int {
        val currentWidth = plain.xMax - plain.xMin  //тек. ширина видимой области
        val zoomLevel = INITIAL_WIDTH / max(currentWidth, 1e-12)
        // zoomLevel - число во сколько раз приблизили
        // max(currentWidth, 1e-12) защита от деления на ноль
        val logZoom = if (zoomLevel <= 1.0) 0.0 else ln(zoomLevel) / ln(2.0)
        // logZoom  = log_2(zoomLevel) = ln(zoomLevel)/ln(2) т.к в котлин нет log_2(x)

        return (100 + 140 * logZoom)
            .toInt()
            .coerceIn(100, 15000)
    }
}