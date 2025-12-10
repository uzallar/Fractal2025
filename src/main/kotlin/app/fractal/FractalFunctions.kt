package app.fractal

//import app.math.Complex
import kotlin.math.abs
//import androidx.compose.ui.graphics.drawscope.DrawScope

typealias FractalFunction = (Double, Double, Int) -> Float

const val escapeRadius = 2.0

object FractalFunctions {
    val mandelbrot: FractalFunction = { xc, yc, maxIterations ->
        val escapeRR = escapeRadius * escapeRadius
        var x = 0.0
        var y = 0.0

        var n = 0
        var xx = x * x
        var yy = y * y

        while (n < maxIterations && xx + yy < escapeRR) {
            val xy = x * y
            y = xy + xy + yc
            x = xx - yy + xc

            xx = x * x
            yy = y * y

            n++
        }

        n.toFloat() / maxIterations
    }


    val julia: FractalFunction =  { _x, _y, maxIterations ->
        val escapeRR = escapeRadius * escapeRadius
        val xc = -0.7
        val yc = 0.27015
        var x = _x
        var y = _y

        var n = 0
        var xx = x * x
        var yy = y * y

        while (n < maxIterations && xx + yy < escapeRR) {
            val xy = x * y
            y = xy + xy + yc
            x = xx - yy + xc

            xx = x * x
            yy = y * y
            n++
        }

        n.toFloat() / maxIterations
    }

    val tricorn: FractalFunction = { xc, yc, maxIterations ->
        val escapeRR = escapeRadius * escapeRadius
        var x = 0.0
        var y = 0.0

        var n = 0
        var xx = x * x
        var yy = y * y

        while (n < maxIterations && xx + yy < escapeRR) {
            val xy = x * y
            y = -xy - xy + yc
            x = xx - yy + xc

            xx = x * x
            yy = y * y
            n++
        }

        n.toFloat() / maxIterations
    }

    val cubicMandelbrot: FractalFunction = { xc, yc, maxIterations ->
        val escapeRR = escapeRadius * escapeRadius
        var x = 0.0
        var y = 0.0

        var n = 0
        var xx = x * x
        var yy = y * y

        while (n < maxIterations && xx + yy < escapeRR) {
            val xy2 = 2.0 * x * y

            x = (xx - yy) * x - xy2 * y + xc
            y = (xx - yy) * y + xy2 * x + yc

            xx = x * x
            yy = y * y

            n++
        }

        n.toFloat() / maxIterations
    }

    fun getFractalByName(name: String): FractalFunction {
        return when (name.lowercase()) {
            "mandelbrot", "мандельброт" -> mandelbrot
            "julia", "жюлиа" -> julia
            "tricorn", "трикорн" -> tricorn
            //"cubic", "кубический" -> cubicMandelbrot
            else -> mandelbrot
        }
    }
}

